This project implements a restaurant simulation using JADE (Java Agent DEvelopment Framework) where multiple autonomous agents work together to manage restaurant operations. The system demonstrates agent cooperation, task delegation, and queue management.
Agent Types
1. Waiter Agent

Role: Manages customer service and coordinates between customers and chef
Responsibilities:

Assigns tables to arriving customers
Takes food orders from customers
Forwards orders to the chef
Serves food when ready
Handles billing when customers leave
Manages table availability



2. Customer Agent

Role: Simulates restaurant customers with autonomous behavior
Responsibilities:

Requests a table upon arrival
Reviews menu and places orders
Waits for food to be served
"Eats" the meal (simulated with time delay)
Requests bill and leaves



3. Chef Agent

Role: Prepares food orders in the kitchen
Responsibilities:

Receives orders from waiter
Manages cooking queue (FIFO)
Simulates cooking time based on dish complexity
Notifies waiter when orders are ready



Key Features
Agent Communication

Uses JADE ACL (Agent Communication Language) messages
Different performatives: REQUEST, INFORM, AGREE, REFUSE, CONFIRM
Structured message content for different interactions

Autonomous Behavior

Customers make independent decisions about orders
Chef manages cooking queue automatically
Waiter coordinates between multiple customers and chef

Resource Management

Limited number of tables (5 tables)
Queue management for cooking orders
Realistic timing for different activities

Realistic Simulation

Variable cooking times for different dishes
Random customer orders from menu
Staggered customer arrivals
Eating time simulation

Workflow

Restaurant Opens: Waiter and Chef agents are created
Customers Arrive: Customer agents created with staggered timing
Table Assignment: Customers request tables from waiter
Ordering: Customers place orders, waiter forwards to chef
Cooking: Chef processes orders in queue with realistic timing
Service: Waiter delivers food when chef finishes cooking
Dining: Customers eat (simulated delay)
Departure: Customers request bill and leave, freeing tables

Multi-agent coordination: See how agents work together
Message passing: Understand ACL communication
Autonomous behavior: Agents make independent decisions
Resource management: Handle limited resources (tables)
Queue processing: Chef manages order queue
Event-driven programming: Agents react to messages
Alice: Ordering Pizza, Salad
Chef: Received order for Alice: Pizza, Salad
Chef: Starting to cook Pizza, Salad for Alice (estimated time: 10 seconds)
