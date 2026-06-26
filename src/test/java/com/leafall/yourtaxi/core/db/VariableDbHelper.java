package com.leafall.yourtaxi.core.db;

import com.leafall.yourtaxi.core.utils.entity.VariableEntityUtils;
import com.leafall.yourtaxi.entity.VariableEntity;
import com.leafall.yourtaxi.entity.enums.VariableType;
import com.leafall.yourtaxi.repository.VariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.leafall.yourtaxi.config.ConstantsConfig.BIG_ORDER_FROM_KEY;
import static com.leafall.yourtaxi.config.ConstantsConfig.PRICE_KEY;

@Component
@RequiredArgsConstructor
public class VariableDbHelper {

    private final VariableRepository variableRepository;

    public VariableEntity save() {
        return variableRepository.save(VariableEntityUtils.generate());
    }

    public List<VariableEntity> saveDefaultVariables() {
        var variable = new VariableEntity();
        variable.setKey(PRICE_KEY);
        variable.setValue("1");
        variable.setType(VariableType.NUMBER);
        var variable2 = new VariableEntity();
        variable2.setKey(BIG_ORDER_FROM_KEY);
        variable2.setValue("10");
        variable2.setType(VariableType.NUMBER);
        return variableRepository.saveAll(Arrays.asList(variable ,variable2));
    }

    public VariableEntity save(VariableEntity variable) {
        return variableRepository.save(variable);
    }
}
