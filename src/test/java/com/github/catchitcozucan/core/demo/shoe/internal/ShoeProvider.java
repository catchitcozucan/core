package com.github.catchitcozucan.core.demo.shoe.internal;


import com.github.catchitcozucan.core.demo.test.support.ArrayRotator;

public class ShoeProvider {

    ArrayRotator<Shoe> SHOES = new ArrayRotator<>(new Shoe[] {
            new Shoe("redlightning", Shoe.HipFactor.DASHIT),
            new Shoe("ashes", Shoe.HipFactor.NONE),
            new Shoe("kalles", Shoe.HipFactor.SOMEWHAT)
    });

    private ShoeProvider(){}

    private static ShoeProvider INSTANCE;
    public static synchronized ShoeProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShoeProvider();
        }
        return INSTANCE;
    }

    public Shoe getShoe(String color, long size) {
        return SHOES.getNextPhading();
    }


}
