
package com.codinglitch.ctweaks.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.tuple.Pair;

public class RandomMap {
    List<Pair<?, Double>> content = new ArrayList<>();
    Random rand = new Random();

    public RandomMap() {
    }

    public RandomMap(Random rand) {
        this.rand = rand;
    }

    public RandomMap(List<Pair<?, Double>> content) {
        this.content = content;
    }

    public RandomMap(List<Pair<?, Double>> content, Random rand) {
        this.content = content;
        this.rand = rand;
    }

    public void add(Pair<?, Double> toAdd) {
        this.content.add(toAdd);
    }

    public <T> T sample() {
        double total = 0.0D;

        for (Pair<?, Double> pair : this.content) {
            total += pair.getValue();
        }

        double chosen = this.rand.nextInt((int)total);
        double chances = 0.0D;

        for (Pair<?, Double> pair : this.content)
        {
            chances += pair.getValue();
            if (chosen < chances)
            {
                return (T) pair.getKey();
            }
        }

        return null;
    }
}
