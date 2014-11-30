package BotBrains;

/**
 * Created by byronandanne on 11/26/2014.
 */
public abstract class Goal {

    private boolean _valueCalc = false;
    private float _value;
    public float getGoalValue(){
        if(!_valueCalc){
            _value = recalculateValue();
            _valueCalc = true;
        }

        return _value;
    }
    public void resetValue(){
        _value = 0;
        _valueCalc = false;
    }

    protected abstract float recalculateValue();
    public abstract float getGoalChange(Action action);
}

