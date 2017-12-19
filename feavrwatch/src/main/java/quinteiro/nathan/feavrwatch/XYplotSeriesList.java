package quinteiro.nathan.feavrwatch;

import com.androidplot.xy.LineAndPointFormatter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by degiovan on 11/11/2017.
 */

public class XYplotSeriesList {

    private ArrayList<Integer[]> xList = new ArrayList<>();
    private ArrayList<Integer[]> yList = new ArrayList<>();
    private ArrayList<List<Number>> xyList = new ArrayList<>();
    private ArrayList<String> xyTagList = new ArrayList<>();
    private ArrayList<LineAndPointFormatter> xyFormatterList = new ArrayList<>();


    public List<Number> getSeriesFromList(String xyTag){
        return xyList.get(xyTagList.indexOf(xyTag));
    }


    public LineAndPointFormatter getFormatterFromList(String xyTag){
        return xyFormatterList.get(xyTagList.indexOf(xyTag));
    }

    public ArrayList<String> getXyTagList() {
        return xyTagList;
    }

    public void initializeSeriesAndAddToList(String xyTag, int CONSTANT, int NUMBER_OF_POINTS, LineAndPointFormatter xyFormatter){
        Integer [] x = new Integer[NUMBER_OF_POINTS];
        Integer [] y = new Integer[NUMBER_OF_POINTS];
        List<Number> xy = new ArrayList<>();

        for (int i=0; i<y.length; i+=1){
            x[i] = i;
            y[i] = CONSTANT;
            xy.add(x[i]);
            xy.add(y[i]);
        }

        xList.add(x);
        yList.add(y);
        xyList.add(xy);
        xyTagList.add(xyTag);
        xyFormatterList.add(xyFormatter);
    }

    public void updateSeries(String xyTag, int data){
        List<Number> xy =  xyList.get(xyTagList.indexOf(xyTag));
        Integer[] x = xList.get(xyTagList.indexOf(xyTag));
        Integer[] y = yList.get(xyTagList.indexOf(xyTag));

        xy.clear();
        for (int i=0; i<y.length-1; i+=1){
            y[i] = y[i+1];
            xy.add(x[i]);
            xy.add(y[i]);
        }
        y[y.length-1] = data;
        xy.add(x[y.length-1]);
        xy.add(y[y.length-1]);

        xyList.set(xyTagList.indexOf(xyTag),xy);
        xList.set(xyTagList.indexOf(xyTag),x);
        yList.set(xyTagList.indexOf(xyTag),y);
    }

    public int getSizeList(){
        return xyList.size();
    }


}
