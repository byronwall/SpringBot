package BotBrains;

import com.springrts.ai.oo.AIFloat3;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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

    float[][] data;

    int width_divisions;
    int height_divisions;

    float map_width;
    float map_height;

    float data_width;
    float data_height;

    String name;

    /**
     * @param name
     * @param map_width  These need to be real values. (Already *8)
     * @param map_height
     */
    public DataMap(String name, float map_width, float map_height) {
        this.name = name;

        this.map_width = map_width;
        this.map_height = map_height;

        //TODO, test these values and un-hardcode if needed
        data_width = 256;
        data_height = 256;


        //determine number of boxes needed
        height_divisions = 1 + ((int) (map_height / data_height));
        width_divisions = 1 + ((int) (map_width / data_width));

        //we now have our box sizes, create data store
        data = new float[height_divisions][width_divisions];

        SpringBot.write("created map: " + this.toString() + dataToString());

    }

    public void toImage(String id) {

        //create a hard coded file
        String path = "C:\\Users\\byronandanne\\Documents\\My Games\\Spring\\AI\\Skirmish\\SpringBot\\0.1\\images\\";

        BufferedImage bi = new BufferedImage((int) map_width / 8, (int) map_height / 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();

        float max = 0;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                if (data[i][j] > max) {
                    max = data[i][j];
                }

            }
        }

        Font font = new Font("Arial", Font.BOLD, 10);
        g.setFont(font);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                //these just scale down the image to be an 1/8 of the size
                int width = (int) data_width / 8;
                int height = (int) data_height / 8;

                int x = j * width;
                int y = i * height;

                int val = (int) (data[i][j] / max * 255);

                g.setColor(new Color(val, val, val));
                g.fillRect(x, y, width, height);

                g.setPaint(Color.WHITE);
                g.drawString((int) data[i][j] + "", x, y);

            }
        }

        g.dispose();

        try {
            ImageIO.write(bi, "PNG", new File(path + id + System.currentTimeMillis() + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }


        //draw the data as gray scale for each square

        //make it 1/8 size of full map

    }

    public void decay() {
        //just subtract 1 or make zero for each value

        float average = 0;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                average += data[i][j];
            }
        }

        average = average / (height_divisions * width_divisions);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] -= average;

                if (data[i][j] < 0) {
                    data[i][j] = 0;
                }
            }
        }
    }

    //assume this is done via addition for now

    public float addToMap(float x, float z, float value) {


        //need to get the box to store in first
        int i = (int) (z / data_height);
        int j = (int) (x / data_width);

        //SpringBot.write("adding to spot: {" + x + "," + z + "} in spot: " + i + "," + j);

        data[i][j] += value;

        return data[i][j];
    }

    public float addToMap(AIFloat3 pos, float value) {
        return addToMap(pos.x, pos.z, value);
    }

    public String dataToString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                sb.append("{" + i + "," + j + ": " + data[i][j] + "}");
            }
        }

        return sb.toString();
    }

    /**
     * Method will return the 9 nearby positions including the current one
     *
     * @param pos
     * @return
     */
    public HashMap<AIFloat3, Float> nearbyValues(AIFloat3 pos, boolean include_corners) {

        HashMap<AIFloat3, Float> grid_values = new HashMap<>();

        //convert pos to box
        int i = (int) (pos.z / data_height);
        int j = (int) (pos.x / data_width);

        //get nearby boxes

        //to right, i+1
        if (j + 1 < width_divisions) {
            grid_values.put(boxToPos(i, j + 1), data[i][j + 1]);
        }

        //to left
        if (j - 1 >= 0) {
            grid_values.put(boxToPos(i, j - 1), data[i][j - 1]);
        }

        //to up
        if (i - 1 >= 0) {
            grid_values.put(boxToPos(i - 1, j), data[i - 1][j]);
        }

        //to down
        if (i + 1 < height_divisions) {
            grid_values.put(boxToPos(i + 1, j), data[i + 1][j]);
        }


        //return map of all areas

        return grid_values;


    }

    public float fractionAbove(float value) {
        int count = 0;
        int total = 0;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                if (data[i][j] > value) {
                    count++;
                }

                total++;

            }
        }

        return 1.0f * count / total;
    }

    //returns a random index if they are all equal to 0.
    public AIFloat3 getHighestValue() {

        float max_value = -Float.MAX_VALUE;
        AIFloat3 max_pos = null;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                if (data[i][j] > max_value) {
                    max_value = data[i][j];
                    max_pos = boxToPos(i, j);
                }
            }
        }

        //have the max return those coordinates
        return max_pos;
    }

    private AIFloat3 boxToPos(int i, int j) {

        float x = (j * data_width + (j + 1) * data_width) / 2;
        float z = (i * data_height + (i + 1) * data_height) / 2;

        return new AIFloat3(x, 0, z);
    }

    public void blur() {
        //copy matrix

        SpringBot.write("doing a blur...");
        SpringBot.write("before: " + dataToString());

        float[][] temp = new float[height_divisions][width_divisions];

        //go through rows and update
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                temp[i][j] = (getValueOrDefault(i, j - 1, 0) + 2 * getValueOrDefault(i, j, 0) + getValueOrDefault(i, j + 1, 0)) / 4.0f;

            }
        }

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {

                temp[i][j] += (getValueOrDefault(i - 1, j, 0) + 2 * getValueOrDefault(i, j, 0) + getValueOrDefault(i + 1, j, 0)) / 4.0f;

            }
        }

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                data[i][j] = temp[i][j];
            }
        }

        SpringBot.write("after: " + dataToString());

        //go through columns and update

        //copy matrix back over
    }

    private float getValueOrDefault(int i, int j, float def) {
        if (i >= 0 && i < height_divisions && j >= 0 && j < width_divisions) {
            return data[i][j];
        } else {
            return def;
        }
    }

    public AIFloat3 getLowestValue() {

        float min_value = Float.MAX_VALUE;
        AIFloat3 min_pos = null;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {


                if (data[i][j] < min_value) {
                    min_value = data[i][j];
                    min_pos = boxToPos(i, j);
                }
            }
        }

        //have the max return those coordinates
        return min_pos;
    }

    @Override
    public String toString() {
        return "DataMap: " + name + ", {data width, height}: {" + data_width + "," + data_height +
                "}, {div width, height} {" + width_divisions + "," + height_divisions + "}";
    }
}
