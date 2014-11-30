package BotBrains.Graph;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public List<Node> connections_in = new ArrayList<Node>();
    public List<Node> connections_out = new ArrayList<Node>();
    public String data;

    public Node(String data) {
        this.data = data;
    }

    public Iterable<Node> getOutbound(){
        return connections_out;
    }

}
