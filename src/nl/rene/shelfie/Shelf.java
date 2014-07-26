package nl.rene.shelfie;

import android.content.Context;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Shelf {
    private static final String FILENAME = "theshelf.json";
    private static final long DAY_DURATION = 86400000;
    private List<ShelfItem> items;
    private List<ShelfItem> groceries;

    private boolean changed = false;
    private static Shelf instance = null;
    private int currentItem = 0;

    private Shelf(String filename, Context context) {
        FileInputStream is;
        this.items = new ArrayList<ShelfItem>();
        this.groceries = new ArrayList<ShelfItem>();
        try {
            is = context.openFileInput(filename);

            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line = r.readLine();
            StringBuilder sb = new StringBuilder();
            while(line != null) {
                sb.append(line);
                line = r.readLine();
            }
            JSONObject me = new JSONObject(sb.toString());
            JSONArray jsonItems = me.getJSONArray("items");
            for(int i = 0; i < jsonItems.length(); i++) {
                JSONObject jsonItem = jsonItems.getJSONObject(i);
                ShelfItem item = new ShelfItem(jsonItem.getString("name"), jsonItem.getInt("desiredAmount"));
                items.add(item);
            }
            long updatedAt = me.getLong("updatedAt");
            if(System.currentTimeMillis() < DAY_DURATION + updatedAt) {
                JSONArray jsonGroceries = me.getJSONArray("groceries");
                for(int i = 0; i < jsonGroceries.length(); i++) {
                    JSONObject jsonGrocery = jsonGroceries.getJSONObject(i);
                    ShelfItem grocery = new ShelfItem(jsonGrocery.getString("name"), jsonGrocery.getInt("desiredAmount"));
                    groceries.add(grocery);
                }
            }
            is.close();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public static Shelf getInstance(Context context) {
        if(instance == null) { instance = new Shelf(FILENAME, context); }
        return instance;
    }


    public List<ShelfItem> getItems() {
        return this.items;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject me = new JSONObject();
        JSONArray jsonItems = new JSONArray();
        JSONArray jsonGroceries = new JSONArray();
        me.put("name", "standard_shelf");
        for(ShelfItem item : items) {
            jsonItems.put(item.toJSON());
        }
        me.put("items", jsonItems);
        for(ShelfItem grocery : groceries) {
            jsonGroceries.put(grocery.toJSON());
        }
        me.put("groceries", jsonGroceries);
        me.put("updatedAt", System.currentTimeMillis());
        return me;
    }

    @Override
    public String toString() {
        return "Shelf{items=" + items + "}";
    }

    public void save(Context context) {
        if(!changed) { return; }
        FileOutputStream os;
        try {
            os = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            Log.d("SHELFIE", "Writing JSON: " + toJSON().toString());
            os.write(toJSON().toString().getBytes());
            changed = false;
            os.close();
        } catch(IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addItem(ShelfItem item) {
        items.add(item);
        setChanged(true);
    }

    public void removeItem(int position) {
        items.remove(position);
        setChanged(true);
    }

    public void setChanged(boolean changed) {
        if(changed) { Log.d("SHELFIE", "Change registered"); }
        this.changed = changed;
    }

    public void adjustDesiredAmount(ShelfItem item, int relAmt) {
        item.adjustDesiredAmount(relAmt);
        setChanged(true);

    }

    public void swapItems(int position, int pos2) {
        if(items.isEmpty() || pos2 < 0 || items.size() <= pos2) { return; }

        ShelfItem a = items.get(position);
        ShelfItem b = items.get(pos2);
        if(a != null && b != null) {
            items.set(pos2, a);
            items.set(position, b);
            setChanged(true);
        }
    }

    public ShelfItem getCurrentItem() {
        return items.get(currentItem);
    }

    public void removeGrocery(int position) {
        if(groceries.get(position) != null) { groceries.remove(position); setChanged(true);}
    }

    public List<ShelfItem> getGroceries() {
        return groceries;
    }

    public void addGrocery(ShelfItem item, int amount) {
        ShelfItem grocery = new ShelfItem(item.getName(), amount);
        groceries.add(grocery);
        setChanged(true);
    }

    public void nextItem() {
        if(currentItem < items.size() - 1) {
            currentItem++;
        }
    }

    public void prevItem() {
        if(currentItem > 0) { currentItem--; }
    }
}