#!/usr/bin/env python3
"""
🛒 Simple E-Commerce System Demo
Shows how distributed microservices work together
"""

import time
import json
from datetime import datetime

class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    END = '\033[0m'
    BOLD = '\033[1m'

class Store:
    def __init__(self):
        # Our store inventory
        self.products = {
            "laptop": {"name": "Gaming Laptop", "price": 800, "stock": 5},
            "phone": {"name": "Smartphone", "price": 500, "stock": 10},
            "tablet": {"name": "Tablet", "price": 300, "stock": 3}
        }
        
        # Customer accounts
        self.customers = {
            "customer1": {"name": "John Doe", "balance": 1000},
            "customer2": {"name": "Jane Smith", "balance": 400},
            "customer3": {"name": "Rich Guy", "balance": 10000}
        }
        
        self.orders = {}
        self.order_counter = 1

    def print_header(self, title):
        print(f"\n{Colors.HEADER}{'='*60}{Colors.END}")
        print(f"{Colors.HEADER}{Colors.BOLD}{title:^60}{Colors.END}")
        print(f"{Colors.HEADER}{'='*60}{Colors.END}")

    def print_step(self, service, message, status="info"):
        timestamp = datetime.now().strftime("%H:%M:%S")
        color = Colors.BLUE
        if status == "success":
            color = Colors.GREEN
        elif status == "error":
            color = Colors.RED
        elif status == "warning":
            color = Colors.YELLOW
            
        print(f"{color}[{timestamp}] {service:15} | {message}{Colors.END}")

    def show_store_status(self):
        self.print_header("🏪 CURRENT STORE STATUS")
        
        print(f"{Colors.BOLD}📦 INVENTORY:{Colors.END}")
        for product_id, product in self.products.items():
            print(f"  {product['name']:15} | ${product['price']:4} | Stock: {product['stock']}")
        
        print(f"\n{Colors.BOLD}💰 CUSTOMER ACCOUNTS:{Colors.END}")
        for customer_id, customer in self.customers.items():
            print(f"  {customer['name']:15} | Balance: ${customer['balance']}")

    def process_order(self, customer_id, product_id, quantity=1):
        order_id = f"ORD-{self.order_counter:03d}"
        self.order_counter += 1
        
        customer = self.customers[customer_id]
        product = self.products[product_id]
        total_amount = product['price'] * quantity
        
        self.print_header(f"🛒 NEW ORDER: {order_id}")
        print(f"{Colors.BOLD}Customer:{Colors.END} {customer['name']}")
        print(f"{Colors.BOLD}Product:{Colors.END} {quantity}x {product['name']}")
        print(f"{Colors.BOLD}Total:{Colors.END} ${total_amount}")
        
        # Step 1: Order Service creates order
        self.print_step("ORDER SERVICE", f"📋 New order {order_id} created", "info")
        self.print_step("ORDER SERVICE", f"📻 Broadcasting to all services...", "info")
        time.sleep(1)
        
        # Step 2: Payment Service checks money
        self.print_step("PAYMENT SERVICE", f"💳 Checking customer balance...", "info")
        time.sleep(1)
        
        payment_ok = customer['balance'] >= total_amount
        if payment_ok:
            self.print_step("PAYMENT SERVICE", f"✅ Payment APPROVED (${customer['balance']} >= ${total_amount})", "success")
            # Reserve money
            customer['balance'] -= total_amount
            self.print_step("PAYMENT SERVICE", f"💰 Reserved ${total_amount}, remaining: ${customer['balance']}", "info")
        else:
            self.print_step("PAYMENT SERVICE", f"❌ Payment REJECTED (${customer['balance']} < ${total_amount})", "error")
        
        time.sleep(1)
        
        # Step 3: Stock Service checks inventory
        self.print_step("STOCK SERVICE", f"📦 Checking inventory...", "info")
        time.sleep(1)
        
        stock_ok = product['stock'] >= quantity
        if stock_ok:
            self.print_step("STOCK SERVICE", f"✅ Stock AVAILABLE ({product['stock']} >= {quantity})", "success")
            # Reserve stock
            product['stock'] -= quantity
            self.print_step("STOCK SERVICE", f"📦 Reserved {quantity} items, remaining: {product['stock']}", "info")
        else:
            self.print_step("STOCK SERVICE", f"❌ Stock UNAVAILABLE ({product['stock']} < {quantity})", "error")
        
        time.sleep(1)
        
        # Step 4: Order Service makes final decision
        self.print_step("ORDER SERVICE", f"🤔 Evaluating responses...", "info")
        time.sleep(1)
        
        if payment_ok and stock_ok:
            # SUCCESS!
            self.print_step("ORDER SERVICE", f"🎉 Order {order_id} CONFIRMED!", "success")
            self.print_step("PAYMENT SERVICE", f"💳 Payment committed", "success")
            self.print_step("STOCK SERVICE", f"📦 Stock committed", "success")
            self.print_step("ORDER SERVICE", f"📧 Confirmation email sent to customer", "success")
            
        elif payment_ok and not stock_ok:
            # Rollback payment
            self.print_step("ORDER SERVICE", f"🚫 Order {order_id} CANCELLED (No stock)", "error")
            customer['balance'] += total_amount
            self.print_step("PAYMENT SERVICE", f"↩️ Payment rolled back, refunded ${total_amount}", "warning")
            
        elif not payment_ok and stock_ok:
            # Rollback stock
            self.print_step("ORDER SERVICE", f"🚫 Order {order_id} CANCELLED (No money)", "error")
            product['stock'] += quantity
            self.print_step("STOCK SERVICE", f"↩️ Stock rolled back, returned {quantity} items", "warning")
            
        else:
            # Both failed
            self.print_step("ORDER SERVICE", f"🚫 Order {order_id} REJECTED (No money & no stock)", "error")
        
        print(f"\n{Colors.BOLD}📊 FINAL STATUS:{Colors.END}")
        print(f"  Customer Balance: ${customer['balance']}")
        print(f"  Product Stock: {product['stock']}")

def main():
    store = Store()
    
    print(f"{Colors.HEADER}{Colors.BOLD}")
    print("🛒 WELCOME TO THE DISTRIBUTED E-COMMERCE DEMO!")
    print("Watch how microservices work together to process orders")
    print(f"{Colors.END}")
    
    while True:
        store.show_store_status()
        
        print(f"\n{Colors.BOLD}🎯 DEMO SCENARIOS:{Colors.END}")
        print("1. 😊 Happy Path - John buys 1 laptop ($800)")
        print("2. 💸 Poor Customer - Jane tries to buy 1 laptop ($800)")  
        print("3. 📦 Out of Stock - Rich Guy tries to buy 10 laptops")
        print("4. 🔄 Reset Store")
        print("5. 🚪 Exit")
        
        choice = input(f"\n{Colors.BOLD}Choose a scenario (1-5): {Colors.END}")
        
        if choice == "1":
            store.process_order("customer1", "laptop", 1)
        elif choice == "2":
            store.process_order("customer2", "laptop", 1)
        elif choice == "3":
            store.process_order("customer3", "laptop", 10)
        elif choice == "4":
            store.__init__()  # Reset
            print(f"{Colors.GREEN}🔄 Store reset to initial state!{Colors.END}")
        elif choice == "5":
            print(f"{Colors.BLUE}👋 Thanks for watching the demo!{Colors.END}")
            break
        else:
            print(f"{Colors.RED}❌ Invalid choice, please try again{Colors.END}")
        
        input(f"\n{Colors.YELLOW}Press Enter to continue...{Colors.END}")

if __name__ == "__main__":
    main()