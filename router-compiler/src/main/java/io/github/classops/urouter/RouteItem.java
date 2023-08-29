package io.github.classops.urouter;

import javax.lang.model.element.TypeElement;
import java.util.Map;

class RouteItem {

    private Byte type;
    private TypeElement typeElement;
    private Map<String, Integer> paramsType;

    public RouteItem(Byte type, TypeElement typeElement, Map<String, Integer> paramsType) {
        this.type = type;
        this.typeElement = typeElement;
        this.paramsType = paramsType;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public Map<String, Integer> getParamsType() {
        return paramsType;
    }

    public void setParamsType(Map<String, Integer> paramsType) {
        this.paramsType = paramsType;
    }
}
