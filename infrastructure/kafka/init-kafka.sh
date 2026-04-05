#!/bin/sh

# List of topics to create
TOPICS="user-events order-events order-item-events payment-events shipping-notifications category-events subsubcategory-events product-general-events batch-detail-events"

echo "Waiting for Kafka to be ready..."

# Loop through and create each topic
for topic in $TOPICS; do
  /opt/kafka/bin/kafka-topics.sh --create --if-not-exists \
    --bootstrap-server kafka:9092 \
    --replication-factor 1 \
    --partitions 3 \
    --topic "$topic"

  if [ $? -eq 0 ]; then
    echo "Successfully created/verified topic: $topic"
  else
    echo "Failed to create topic: $topic"
  fi
done

echo "Kafka initialization complete."
