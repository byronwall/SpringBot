package BotBrains.Graph;

import java.util.HashMap;
import java.util.HashSet;

public class Graph{
    public HashMap<String, Node> nodes = new HashMap<String, Node>();
    HashSet<Connection> connections = new HashSet<Connection>();

    public Node addNode(String data){
        return nodes.put(data, new Node(data));
    }

    public Node addNode(Node node){
        return nodes.put(node.data, node);
    }

    public void addConnection(String source, String target){

        Node node_source;
        if(nodes.containsKey(source)){
            node_source = nodes.get(source);
        }
        else{
            node_source = new Node(source); 
            addNode(node_source);
        }

        Node node_target;
        if(nodes.containsKey(target)){
            node_target = nodes.get(target);
        }
        else{
            node_target = new Node(target); 
            addNode(node_target);
        }


        Connection e = new Connection(node_source, node_target);

        //add the connections all around
        connections.add(e);
        e.source.connections_out.add(node_target);
        e.target.connections_in.add(node_source);
    }
    public boolean nodeExists(String data){
        return nodes.containsKey(data);
    }
}
