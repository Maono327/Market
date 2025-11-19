package com.maono.marketapplication.services.util;

import com.maono.marketapplication.util.ProductActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductActionStrategy {

    private final Map<ProductActionType, Strategy> actionTypeStrategies;

    @Autowired
    public ProductActionStrategy(List<Strategy> strategies) {
        actionTypeStrategies = new HashMap<>();
        for(Strategy strategy : strategies) {
            actionTypeStrategies.put(strategy.getType(), strategy);
        }
    }

    public Mono<Void> execute(ProductActionType actionType, Long id) {
        return actionTypeStrategies.get(actionType).executeChange(id);
    }
}
