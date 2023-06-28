package com.example.beebliotheca.object;

import java.util.Comparator;

public class accessTimeComparator implements Comparator<History> {

    @Override
    public int compare(History o1, History o2) {
        return o2.accessTime.compareTo(o1.accessTime);
    }
}
