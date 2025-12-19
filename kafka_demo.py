#!/usr/bin/env python3
"""
🎭 Kafka Demo - Send Real Messages to Kafka
This simulates what our microservices do
"""

import json
import time
from datetime import datetime
import subprocess
import sys

def send_kafka_message(topic, key, message):
    """Send a message to Kafka using docker exec"""
    try:
        # Convert message to JSON string
        json_message = json.dumps(message)
        
        # Use docker exec to send message via kafka console producer
        cmd = [
            'docker', 'exec', '-i', 'kafka',
            'kafka-console-producer.sh',
            '--bootstrap-server', 'localhost:9092',
            '--topic', topic,
            '--property', f'key.separator=:',
            '--property', 'parse.key=true'
        ]
        
        # Send the message
        input_data = f"{key}:{json_message}\n"
        result = subprocess.run(cmd, input=input_data, text=True, capture_output=True)
        
        if result.returncode == 0:
            print(f"✅ Sent to topic '{topic}': {key} -> {json_message}")
        else:
            print(f"❌ Failed to send message: {result.stderr}")
            
    except Exception as e:
        print(f"❌ Error sending message: {e}")

def create_topics():
    """Create the topics our services use"""
    topics = ['orders', 'payment-events', 'stock-events']
    
    for topic in topics:
        try:
            cmd = [
                'docker', 'exec', 'kafka',
                'kafka-topics.sh',
                '--bootstrap-server', 'localhost:9092',
                '--create', '--topic', topic,
                '--partitions', '1',
                '--replication-factor', '1',
                '--if-not-exists'
            ]
            
            result = subprocess.run(cmd, capture_output=True, text=True)
            if result.returncode == 0:
                print(f"📝 Topic '{topic}' ready")
            else:
                print(f"⚠️  Topic '{topic}' might already exist")
                
        except Exception as e:
            print(f"❌ Error creating topic {topic}: {e}")

def simulate_order_flow():
    """Simulate a complete order flow with real Kafka messages"""
    
    print("\n🎬 SIMULATING REAL ORDER FLOW")
    print("=" * 50)
    
    order_id = f"ORD-{int(time.time())}"
    customer_id = "customer-123"
    
    # Step 1: Order Service creates new order
    print(f"\n📋 Step 1: Order Service creates order {order_id}")
    order_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "NEW",
        "source": "order-service",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("orders", order_id, order_event)
    time.sleep(2)
    
    # Step 2: Payment Service responds
    print(f"\n💳 Step 2: Payment Service processes payment")
    payment_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "ACCEPT",
        "source": "payment-service",
        "transactionId": f"PAY-{int(time.time())}",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("payment-events", order_id, payment_event)
    time.sleep(2)
    
    # Step 3: Stock Service responds
    print(f"\n📦 Step 3: Stock Service checks inventory")
    stock_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "ACCEPT",
        "source": "stock-service",
        "transactionId": f"STK-{int(time.time())}",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("stock-events", order_id, stock_event)
    time.sleep(2)
    
    # Step 4: Order Service sends final confirmation
    print(f"\n🎉 Step 4: Order Service confirms order")
    confirmation_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "CONFIRMATION",
        "source": "order-service",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("orders", order_id, confirmation_event)
    
    print(f"\n✨ Order {order_id} completed successfully!")
    print(f"\n🔍 Now check Kafka UI at: http://localhost:8090")
    print("   You should see messages in:")
    print("   - 📝 orders topic (2 messages)")
    print("   - 💳 payment-events topic (1 message)")
    print("   - 📦 stock-events topic (1 message)")

def simulate_failed_order():
    """Simulate a failed order (insufficient funds)"""
    
    print("\n🚫 SIMULATING FAILED ORDER (No Money)")
    print("=" * 50)
    
    order_id = f"ORD-FAIL-{int(time.time())}"
    customer_id = "poor-customer"
    
    # Step 1: New order
    print(f"\n📋 Step 1: Order Service creates order {order_id}")
    order_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "NEW",
        "source": "order-service",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("orders", order_id, order_event)
    time.sleep(2)
    
    # Step 2: Payment Service REJECTS
    print(f"\n💸 Step 2: Payment Service REJECTS (insufficient funds)")
    payment_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "REJECT",
        "source": "payment-service",
        "reason": "Insufficient funds",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("payment-events", order_id, payment_event)
    time.sleep(2)
    
    # Step 3: Stock Service still responds (but will be rolled back)
    print(f"\n📦 Step 3: Stock Service accepts (but will rollback)")
    stock_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "ACCEPT",
        "source": "stock-service",
        "transactionId": f"STK-{int(time.time())}",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("stock-events", order_id, stock_event)
    time.sleep(2)
    
    # Step 4: Order Service sends ROLLBACK
    print(f"\n🔄 Step 4: Order Service sends ROLLBACK")
    rollback_event = {
        "orderId": order_id,
        "customerId": customer_id,
        "products": {"laptop": 1},
        "totalAmount": 800.00,
        "status": "ROLLBACK",
        "source": "order-service",
        "reason": "Payment failed",
        "timestamp": datetime.now().isoformat()
    }
    
    send_kafka_message("orders", order_id, rollback_event)
    
    print(f"\n❌ Order {order_id} failed and rolled back!")

def main():
    print("🎭 KAFKA UI DEMO - LIVE MESSAGE FLOW")
    print("=" * 60)
    print("This will send REAL messages to Kafka that you can see in Kafka UI!")
    print(f"🌐 Open Kafka UI: http://localhost:8090")
    print("=" * 60)
    
    # Create topics first
    print("\n📝 Creating Kafka topics...")
    create_topics()
    
    while True:
        print("\n🎯 DEMO OPTIONS:")
        print("1. 😊 Simulate Successful Order")
        print("2. 💸 Simulate Failed Order (No Money)")
        print("3. 📦 Simulate Out of Stock Order")
        print("4. 🔍 Check Kafka UI (http://localhost:8090)")
        print("5. 🚪 Exit")
        
        choice = input("\nChoose option (1-5): ")
        
        if choice == "1":
            simulate_order_flow()
        elif choice == "2":
            simulate_failed_order()
        elif choice == "3":
            print("📦 Out of stock simulation - similar to failed order but stock service rejects")
        elif choice == "4":
            print(f"\n🌐 Open your browser to: http://localhost:8090")
            print("   Click on 'Topics' to see all the message topics")
            print("   Click on any topic to see the messages inside!")
        elif choice == "5":
            print("👋 Demo finished!")
            break
        else:
            print("❌ Invalid choice")
        
        if choice in ["1", "2", "3"]:
            input("\n⏸️  Press Enter to continue (check Kafka UI first!)...")

if __name__ == "__main__":
    main()