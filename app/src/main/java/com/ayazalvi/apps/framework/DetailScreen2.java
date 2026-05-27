package com.ayazalvi.apps.framework;

import com.ayazalvi.apps.framework.databinding.LayoutDetailBinding;
import kotlin.Unit;

public class DetailScreen2 extends Screen<LayoutDetailBinding> {

    ScreenState<String> s = FrameworkKt.state(this, "POP", getArguments().getString("FINAL_COUNT"));

    public DetailScreen2 (ScreenContext context) { super(context); }

    public void onUI() {
        s.bind(getUi().text, (data, view) -> { view.setText(data); return Unit.INSTANCE; });
        getUi().btn.setOnClickListener(v -> getNavigator().pop());
    }

}
