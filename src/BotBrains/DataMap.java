package BotBrains;

import com.springrts.ai.oo.AIFloat3;

/**
 * Created by byronandanne on 11/29/2014.
 */
public class DataMap {

    //data map needs to include

    //flat array of data
    //ability to take coord and put them into array
    //ability to return best/worst locations for action (return center of area)
    //some idea of resolution -> translate world coord to data coord

    //for now data will just store floats

    float[] data;
    int long_divisions;
    int short_divisions;

    int width_divisions;
    int height_divisions;

    float map_width;
    float map_height;

    float data_width;
    float data_height;

    String name;

    public DataMap(String name, int long_divisions, float map_width, float map_height) {
        this.name = name;
        this.long_divisions = long_divisions;
        this.map_width = map_width;
        this.map_height = map_height;

        //determine size of boxes
        if (map_height > map_width) {
            //long = height
            data_height = map_height / long_divisions;
            short_divisions = (int) (map_width / data_height);
            data_width = map_width / short_divisions;

            height_divisions = long_divisions;
            width_divisions = short_divisions;
        } else {
            //long = width
            data_width = map_width / long_divisions;
            short_divisions = (int) (map_height / data_width);
            data_height = map_height / short_divisions;

            height_divisions = short_divisions;
            width_divisions = long_divisions;
        }

        //we now have our box sizes, create data store
        data = new float[height_divisions * width_divisions];

        //init with random values
        for (int i = 0; i < data.length; i++) {
            data[i] = Util.RAND.nextInt(10);
        }


        SpringBot.write("created map: " + this.toString() + dataToString());
        SpringBot.write("data length: " + data.length);
    }

    public void decay(){
        //just subtract 1 or make zero for each value
        for (int i = 0; i < data.length; i++) {
            if(data[i]>0) data[i]--;
        }
    }

    //assume this is done via addition for now
    public float addToMap(float x, float z, float value) {


        //need to get the box to store in first
        int box = ((int) (z / data_height)) * width_divisions + (int) (x / data_width);

        SpringBot.write("adding to spot: {" +x+","+z+"} in spot: " + box);

        data[box] += value;

        return data[box];
    }

    public float addToMap(AIFloat3 pos, float value) {
        return addToMap(pos.x, pos.z, value);
    }

    public String dataToString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            float v = data[i];

            sb.append("{" + i + ": " + v + "}");
        }

        return sb.toString();
    }

    //returns a random index if they are all equal to 0.
    public AIFloat3 getHighestValue() {

        float max_value = -Float.MAX_VALUE;
        int max_index = Util.RAND.nextInt(data.length);
        for (int i = 0; i < data.length; i++) {
            float v = data[i];

            if (v > max_value) {
                max_value = v;
                max_index = i;
            }
        }

        //have the max return those coordinates
        int x_box = max_index % width_divisions;
        int z_box = max_index % height_divisions;

        //pick out the center of the area
        float x = (x_box * data_width + (x_box + 1) * data_width) / 2.0f;
        float z = (z_box * data_height + (z_box + 1) * data_height) / 2.0f;

        return new AIFloat3(x, 0, z);
    }

    public AIFloat3 getLowestValue() {

        float min_value = Float.MAX_VALUE;
        int min_index = Util.RAND.nextInt(data.length);
        for (int i = 0; i < data.length; i++) {
            float v = data[i];

            if (v < min_value) {
                min_value = v;
                min_index = i;
            }
        }

        //have the max return those coordinates
        int x_box = min_index % width_divisions;
        int z_box = min_index % height_divisions;

        //pick out the center of the area
        float x = (x_box * data_width + (x_box + 1) * data_width) / 2.0f;
        float z = (z_box * data_height + (z_box + 1) * data_height) / 2.0f;

        return new AIFloat3(x, 0, z);
    }

    @Override
    public String toString() {
        return "DataMap: " + name + ", {data width, height}: {" + data_width + "," + data_height +
                "}, {div width, height} {" + width_divisions + "," + height_divisions + "}";
    }
}
