package progettoDS2.utils;


public class ValueStorage {
  int count;
  double cumul;
  double maxValue;
  double minValue;
  boolean firstValue;
  public ValueStorage() {
    count = 0;
    cumul = 0.0;
    firstValue = true;
  }
  public void addCumul(double value) {
    cumul += value;
    count ++;
    if(firstValue) {
      maxValue = value;
      minValue = value;
      firstValue = false;
    }
    maxValue = maxValue > value ? maxValue : value;
    minValue = minValue < value ? minValue : value;
  }
  public double getValue() {
    return count != 0 ? cumul / (double)count : 0;
  }
  public double getMaxValue() {
    return maxValue;
  }
  public double getMinValue() {
    return minValue;
  }
}
