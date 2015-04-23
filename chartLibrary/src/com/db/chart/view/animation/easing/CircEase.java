package com.db.chart.view.animation.easing;

public class CircEase extends BaseEasingMethod {

    public CircEase() {
        super();
    }

    @Override
    protected float easeOut(float time) {
        return 1 * (float) Math.sqrt(1 - (time = time - 1) * time);
    }

    @Override
    protected float easeIn(float time) {
        return easeOut(1.f - time);
    }

}
