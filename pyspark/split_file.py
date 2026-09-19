import argparse
import logging
import os

from pyspark.sql import SparkSession


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

logger = logging.getLogger(__name__)


def split_csv(spark, input_file, output_directory):
    logger.info("Starting CSV file split")

    df = (
        spark.read
        .option("header", True)
        .option("inferSchema", False)
        .csv(input_file)
    )

    record_count = df.count()

    logger.info("Input file: %s", input_file)
    logger.info("Total records: %s", record_count)

    (
        df.write
        .mode("overwrite")
        .option("header", True)
        .csv(output_directory)
    )

    logger.info("CSV file split completed successfully")
    logger.info("Output directory: %s", output_directory)


def split_json(spark, input_file, output_directory):
    logger.info("Starting JSON file split")

    df = spark.read.json(input_file)

    record_count = df.count()

    logger.info("Input file: %s", input_file)
    logger.info("Total records: %s", record_count)

    def create_json_array(iterator):
        import json

        records = list(iterator)

        if records:
            yield json.dumps(
                [record.asDict(recursive=True) for record in records]
            )

    (
        df.rdd
        .mapPartitions(create_json_array)
        .saveAsTextFile(output_directory)
    )

    logger.info("JSON file split completed successfully")
    logger.info("Output directory: %s", output_directory)


def main():
    parser = argparse.ArgumentParser(
        description="Split large CSV or JSON files using PySpark."
    )

    parser.add_argument(
        "--input",
        required=True,
        help="Input file location"
    )

    parser.add_argument(
        "--output",
        required=True,
        help="Output directory for split files"
    )

    parser.add_argument(
        "--format",
        required=True,
        choices=["csv", "json"],
        help="Input file format"
    )

    args = parser.parse_args()

    if not os.path.isfile(args.input):
        raise FileNotFoundError(
            f"Input file does not exist: {args.input}"
        )

    spark = (
        SparkSession.builder
        .appName("LumiFileSplitter")
        .master("local[*]")
        .config(
            "spark.sql.files.maxPartitionBytes",
            1024 * 1024
        )
        .getOrCreate()
    )

    try:
        if args.format == "csv":
            split_csv(
                spark,
                args.input,
                args.output
            )

        elif args.format == "json":
            split_json(
                spark,
                args.input,
                args.output
            )

    finally:
        spark.stop()


if __name__ == "__main__":
    main()