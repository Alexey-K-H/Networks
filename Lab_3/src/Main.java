public class Main {
    public static void main(String[] args){
        ChatNode chatNode = null;

        if (args.length == 3) {
            chatNode = new ChatNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));

        } else if (args.length == 5) {
            chatNode = new ChatNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));

        } else {
            System.out.println("Invalid input arguments. Usage: \n"
                    + "1. node name \n"
                    + "2. loss percentage \n"
                    + "3. node own port \n"
                    + "4. neighbour ip [optional] \n"
                    + "5. neighbour port [optional] \n");
        }

        if (null != chatNode) {
            chatNode.startChatting();
        }
    }
}
