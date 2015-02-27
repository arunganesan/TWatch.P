package edu.umich.eecs.twatchp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by Arun on 2/12/2015.
 */
public class AudioFuncs {
    public static Object[] pcmToDouble(ArrayList<Byte> array) {
        byte[] prim = new byte[array.size()];
        for (int i = 0; i < array.size(); i++) prim[i] = array.get(i);
        ByteBuffer bb = ByteBuffer.wrap(prim);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        short[] shorts_c1 = new short[array.size()/4];
        short[] shorts_c2 = new short[array.size()/4];

        int maxC1 = 0, maxC2 = 0;
        short c1, c2;
        for (int i = 0; i < array.size()/4; i++) {
            c1 = bb.getShort();
            c2 = bb.getShort();

            shorts_c1[i] = c1;
            shorts_c2[i] = c2;

            if (Math.abs(c1) > maxC1) maxC1 = Math.abs(c1);
            if (Math.abs(c2) > maxC2) maxC2 = Math.abs(c2);
        }

        double [] channel1 = new double[shorts_c1.length];
        double [] channel2 = new double[shorts_c2.length];
        for (int i = 0; i < shorts_c1.length; i++) {
            channel1[i] = ((double)shorts_c1[i])/((double)maxC1);
            channel2[i] = ((double)shorts_c2[i])/((double)maxC2);
        }

        return new Object[] {channel1, channel2};
    }
}
