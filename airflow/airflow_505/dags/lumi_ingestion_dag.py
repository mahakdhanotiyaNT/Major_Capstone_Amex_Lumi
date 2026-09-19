from airflow import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.utils.trigger_rule import TriggerRule

from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

import subprocess
import os
import glob
import logging
import psycopg2


BEAM_JAR = "/opt/airflow/beam/beam-ingestion-1.0-SNAPSHOT.jar"

INPUT_DIRECTORY = "/opt/airflow/input"
ERROR_DIRECTORY = "/opt/airflow/errors"

MAX_PARALLEL_CHUNKS = 4

DB_HOST = os.getenv("DB_HOST")
DB_PORT = int(os.getenv("DB_PORT"))
DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")


logger = logging.getLogger(__name__)


def start_ingestion(**context):

    dag_run = context["dag_run"]

    execution_id = dag_run.conf.get("execution_id")

    if not execution_id:
        raise ValueError(
            "execution_id was not provided in DAG configuration"
        )

    logger.info(
        "Ingestion started. Execution ID: %s",
        execution_id
    )


def validate_input_file(**context):

    dag_run = context["dag_run"]

    file_location = dag_run.conf.get("file_location")
    file_locations = dag_run.conf.get("file_locations")

    if file_locations:

        logger.info(
            "Large file ingestion detected. Number of chunks: %s",
            len(file_locations)
        )

        for file_path in file_locations:

            if not os.path.exists(file_path):
                raise FileNotFoundError(
                    f"Input chunk does not exist: {file_path}"
                )

            logger.info(
                "Input chunk exists: %s",
                file_path
            )

        return "large"

    if file_location:

        if not os.path.exists(file_location):
            raise FileNotFoundError(
                f"Input file does not exist: {file_location}"
            )

        logger.info(
            "Input file exists: %s",
            file_location
        )

        return "small"

    raise ValueError(
        "Neither file_location nor file_locations was provided"
    )


def check_processing_type(**context):

    dag_run = context["dag_run"]

    file_locations = dag_run.conf.get("file_locations")

    if file_locations:

        logger.info(
            "Processing type: LARGE FILE"
        )

        return "process_chunks"

    logger.info(
        "Processing type: SMALL FILE"
    )

    return "run_beam_ingestion"


def process_chunks(**context):

    dag_run = context["dag_run"]

    file_locations = dag_run.conf.get("file_locations")

    if not file_locations:
        raise ValueError(
            "file_locations was not provided for large file processing"
        )

    logger.info(
        "Verifying %s input chunks",
        len(file_locations)
    )

    for file_path in file_locations:

        if not os.path.exists(file_path):
            raise FileNotFoundError(
                f"Chunk does not exist: {file_path}"
            )

        logger.info(
            "Chunk ready for Beam ingestion: %s",
            file_path
        )

    logger.info(
        "All chunks verified successfully"
    )


def run_beam_for_file(
        file_location,
        execution_id,
        error_file
):

    logger.info(
        "Starting Beam ingestion for file: %s",
        file_location
    )

    command = [
        "java",
        "-jar",
        BEAM_JAR,
        f"--fileLocation={file_location}",
        f"--executionId={execution_id}",
        f"--errorFile={error_file}"
    ]

    logger.info(
        "Executing Beam command for file: %s",
        file_location
    )

    result = subprocess.run(
        command,
        capture_output=True,
        text=True
    )

    if result.stdout:

        logger.info(
            "Beam STDOUT for %s:\n%s",
            file_location,
            result.stdout
        )

    if result.stderr:

        logger.warning(
            "Beam STDERR for %s:\n%s",
            file_location,
            result.stderr
        )

    if result.returncode != 0:

        logger.error(
            "Beam ingestion failed for file: %s",
            file_location
        )

        raise RuntimeError(
            f"Beam ingestion failed for file: {file_location}"
        )

    logger.info(
        "Beam ingestion completed successfully for file: %s",
        file_location
    )


def run_beam_ingestion(**context):

    dag_run = context["dag_run"]

    execution_id = dag_run.conf.get("execution_id")

    file_location = dag_run.conf.get("file_location")

    file_locations = dag_run.conf.get("file_locations")

    control_file_location = dag_run.conf.get(
        "control_file_location"
    )

    if not control_file_location:

        raise ValueError(
            "control_file_location was not provided"
        )

    if not os.path.exists(control_file_location):

        raise FileNotFoundError(
            f"Control file does not exist: "
            f"{control_file_location}"
        )

    logger.info(
        "Control file received: %s",
        control_file_location
    )

    if not execution_id:

        raise ValueError(
            "execution_id was not provided"
        )

    os.makedirs(
        ERROR_DIRECTORY,
        exist_ok=True
    )

    if file_locations:

        logger.info(
            "Starting Beam ingestion for %s chunks",
            len(file_locations)
        )

        futures = []

        with ThreadPoolExecutor(
            max_workers=MAX_PARALLEL_CHUNKS
        ) as executor:

            for index, chunk_path in enumerate(
                file_locations
            ):

                error_file = os.path.join(
                    ERROR_DIRECTORY,
                    f"error_records_{execution_id}_{index}"
                )

                future = executor.submit(
                    run_beam_for_file,
                    chunk_path,
                    execution_id,
                    error_file
                )

                futures.append(future)

            for future in as_completed(futures):

                future.result()

        logger.info(
            "All Beam chunks completed successfully"
        )

    else:

        if not file_location:

            raise ValueError(
                "file_location was not provided"
            )

        error_file = os.path.join(
            ERROR_DIRECTORY,
            f"error_records_{execution_id}"
        )

        run_beam_for_file(
            file_location,
            execution_id,
            error_file
        )


def check_error_records(**context):

    dag_run = context["dag_run"]

    execution_id = dag_run.conf.get(
        "execution_id"
    )

    pattern = os.path.join(
        ERROR_DIRECTORY,
        f"error_records_{execution_id}*"
    )

    error_files = glob.glob(pattern)

    logger.info(
        "Checking error files for execution ID: %s",
        execution_id
    )

    for error_file in error_files:

        if os.path.isfile(error_file):

            if os.path.getsize(error_file) > 0:

                logger.warning(
                    "Invalid records found in error file: %s",
                    error_file
                )

                return "report_validation_errors"

    logger.info(
        "No validation errors found"
    )

    return "all_records_valid"


def all_records_valid(**context):

    logger.info(
        "All records passed Beam validation"
    )


def report_validation_errors(**context):

    logger.warning(
        "Some records failed validation. "
        "Error files were generated."
    )


def validate_record_count(**context):

    dag_run = context["dag_run"]

    execution_id = dag_run.conf.get(
        "execution_id"
    )

    control_file_location = dag_run.conf.get(
        "control_file_location"
    )

    logger.info(
        "Starting record count validation for execution ID: %s",
        execution_id
    )

    if not execution_id:

        raise ValueError(
            "execution_id was not provided"
        )

    if not control_file_location:

        raise ValueError(
            "control_file_location was not provided"
        )

    if not os.path.exists(control_file_location):

        raise FileNotFoundError(
            f"Control file does not exist: "
            f"{control_file_location}"
        )

    logger.info(
        "Reading control file: %s",
        control_file_location
    )

    expected_record_count = None

    with open(
        control_file_location,
        "r",
        encoding="utf-8"
    ) as control_file:

        for line in control_file:

            line = line.strip()

            if not line or line.startswith("#"):
                continue

            if "=" not in line:
                continue

            key, value = line.split("=", 1)

            if key.strip() == "record_count":

                try:

                    expected_record_count = int(
                        value.strip()
                    )

                except ValueError:

                    raise ValueError(
                        "record_count must be a valid integer"
                    )

                break

    if expected_record_count is None:

        raise ValueError(
            "Control file does not contain record_count"
        )

    logger.info(
        "Expected record count from control file: %s",
        expected_record_count
    )

    query = """
        SELECT COUNT(*)
        FROM employees
        WHERE execution_id = %s
    """

    connection = None

    try:

        logger.info(
            "Connecting to PostgreSQL for actual record count"
        )

        connection = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )

        logger.info(
            "PostgreSQL connection successful"
        )

        with connection.cursor() as cursor:

            cursor.execute(
                query,
                (execution_id,)
            )

            actual_record_count = cursor.fetchone()[0]

        logger.info(
            "Actual loaded record count from PostgreSQL: %s",
            actual_record_count
        )

    except Exception:

        logger.exception(
            "Failed to retrieve actual record count from PostgreSQL"
        )

        raise

    finally:

        if connection is not None:

            connection.close()

            logger.info(
                "PostgreSQL connection closed"
            )

    logger.info(
        "Expected record count: %s",
        expected_record_count
    )

    logger.info(
        "Actual record count: %s",
        actual_record_count
    )

    if actual_record_count != expected_record_count:

        logger.error(
            "Record count validation FAILED. "
            "Expected: %s, Actual: %s",
            expected_record_count,
            actual_record_count
        )

        raise ValueError(
            "Record count mismatch. "
            f"Expected: {expected_record_count}, "
            f"Actual: {actual_record_count}"
        )

    logger.info(
        "Record count validation PASSED. "
        "Expected and actual record counts match."
    )


def ingestion_completed(**context):

    logger.info(
        "INGESTION COMPLETED SUCCESSFULLY"
    )


with DAG(
    dag_id="lumi_ingestion_dag",
    start_date=datetime(2024, 1, 1),
    schedule=None,
    catchup=False,
    tags=[
        "amex",
        "lumi",
        "ingestion"
    ]
) as dag:

    start_ingestion_task = PythonOperator(
        task_id="start_ingestion",
        python_callable=start_ingestion
    )

    validate_input_file_task = PythonOperator(
        task_id="validate_input_file",
        python_callable=validate_input_file
    )

    check_processing_type_task = BranchPythonOperator(
        task_id="check_processing_type",
        python_callable=check_processing_type
    )

    process_chunks_task = PythonOperator(
        task_id="process_chunks",
        python_callable=process_chunks
    )

    run_beam_ingestion_task = PythonOperator(
        task_id="run_beam_ingestion",
        python_callable=run_beam_ingestion,
        trigger_rule=TriggerRule.NONE_FAILED_MIN_ONE_SUCCESS
    )

    check_error_records_task = BranchPythonOperator(
        task_id="check_error_records",
        python_callable=check_error_records
    )

    all_records_valid_task = PythonOperator(
        task_id="all_records_valid",
        python_callable=all_records_valid
    )

    report_validation_errors_task = PythonOperator(
        task_id="report_validation_errors",
        python_callable=report_validation_errors
    )

    validate_record_count_task = PythonOperator(
        task_id="validate_record_count",
        python_callable=validate_record_count,
        trigger_rule=TriggerRule.NONE_FAILED_MIN_ONE_SUCCESS
    )

    ingestion_completed_task = PythonOperator(
        task_id="ingestion_completed",
        python_callable=ingestion_completed,
        trigger_rule=TriggerRule.NONE_FAILED_MIN_ONE_SUCCESS
    )

    start_ingestion_task \
        >> validate_input_file_task \
        >> check_processing_type_task

    check_processing_type_task \
        >> run_beam_ingestion_task

    check_processing_type_task \
        >> process_chunks_task \
        >> run_beam_ingestion_task

    run_beam_ingestion_task \
        >> check_error_records_task

    check_error_records_task \
        >> all_records_valid_task

    check_error_records_task \
        >> report_validation_errors_task

    all_records_valid_task \
        >> validate_record_count_task

    report_validation_errors_task \
        >> validate_record_count_task

    validate_record_count_task \
        >> ingestion_completed_task