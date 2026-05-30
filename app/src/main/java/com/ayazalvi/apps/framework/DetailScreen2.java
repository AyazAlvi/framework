package com.ayazalvi.apps.framework;

import com.ayazalvi.apps.framework.databinding.LayoutDetailBinding;
import com.ayazalvi.framework.core.screen.Screen;
import com.ayazalvi.framework.core.screen.ScreenContext;
import com.ayazalvi.framework.core.screen.ScreenState;
import com.ayazalvi.framework.core.screen.ScreenStateKt;

import kotlin.Unit;

public class DetailScreen2 extends Screen<LayoutDetailBinding> {

    ScreenState<String> s = ScreenStateKt.state(this, "POP", getArguments().getString("FINAL_COUNT"));

    public DetailScreen2 (ScreenContext context) { super(context); }

    public void onUI() {
        s.bind(getUi().text, (data, view) -> { view.setText(data); return Unit.INSTANCE; });
        getUi().btn.setOnClickListener(v -> getNavigator().pop());
    }

}
