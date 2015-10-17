package ru.dyatel.tsuschedule;

import android.content.Context;
import android.util.SparseArray;
import ru.dyatel.tsuschedule.parsing.Parity;

import java.util.HashMap;
import java.util.Map;

public class ParityReference {

    private static final SparseArray<Parity> indexToParity = new SparseArray<Parity>();
    private static final Map<Parity, String> parityToString = new HashMap<Parity, String>();

    static {
        indexToParity.put(0, Parity.ODD);
        indexToParity.put(1, Parity.EVEN);
    }

    private static boolean initialized = false;

    public static void init(Context context) {
        parityToString.put(Parity.ODD, context.getString(R.string.odd_week));
        parityToString.put(Parity.EVEN, context.getString(R.string.even_week));

        initialized = true;
    }

    public static Parity getParityFromIndex(int index) {
        return indexToParity.get(index);
    }

    public static int getIndexFromParity(Parity parity) {
        for (int i = 0; i < indexToParity.size(); i++) {
            if (parity.equals(indexToParity.valueAt(i))) return indexToParity.keyAt(0);
        }
        return -1;
    }

    public static String getStringFromParity(Parity parity) {
        check();
        return parityToString.get(parity);
    }

    public static String getStringFromIndex(int index) {
        return getStringFromParity(getParityFromIndex(index));
    }

    private static void check() {
        if (!initialized) throw new IllegalStateException("ParityReference is not initialized!");
    }

}
