package restaurant;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class RestaurantMain {
    public static void main(String[] args) {
        // Get JADE runtime
        Runtime rt = Runtime.instance();

        // Create main container
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "1099");
        profile.setParameter(Profile.GUI, "true");

        AgentContainer mainContainer = rt.createMainContainer(profile);

        try {
            // Create Waiter Agent
            AgentController waiter = mainContainer.createNewAgent(
                    "Waiter",
                    "restaurant.WaiterAgent",
                    null
            );
            waiter.start();

            // Create Chef Agent
            AgentController chef = mainContainer.createNewAgent(
                    "Chef",
                    "restaurant.ChefAgent",
                    null
            );
            chef.start();

            // Wait for restaurant staff to be ready
            Thread.sleep(2000);

            // Create Customer Agents
            String[] customerNames = {"Alice", "Bob", "Charlie", "David", "Eve"};

            for (int i = 0; i < customerNames.length; i++) {
                AgentController customer = mainContainer.createNewAgent(
                        customerNames[i],
                        "restaurant.CustomerAgent",
                        null
                );
                customer.start();

                // Stagger customer arrivals
                Thread.sleep(3000);
            }

            // Keep the restaurant open for a while
            Thread.sleep(60000); // 1 minute

            System.out.println("\n=== Restaurant is closing ===\n");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}