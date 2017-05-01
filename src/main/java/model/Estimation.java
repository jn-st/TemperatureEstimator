package main.java.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class Estimation {

    private static final BigDecimal SQRT_DIG = new BigDecimal(150);
    private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());

    private static final BigDecimal sigma = new BigDecimal(5.6703 * Math.pow(10.0, -5.0));

    private double mass;
    private double distance;
    private double albedo;
    private double greenhouse;

    private BigDecimal temperature;

    public Estimation() {
    }

    public Estimation(double mass, double distance, double albedo, double greenhouse) {

        this.mass = mass;
        this.distance = distance;
        this.albedo = albedo;
        this.greenhouse = greenhouse;
    }

    public void setVariables(double mass, double distance, double albedo, double greenhouse) {
        this.mass = mass;
        this.distance = distance;
        this.albedo = albedo;
        this.greenhouse = greenhouse;
    }

    public void evaluate() {
        // I have no idea how to do this
        BigDecimal L = new BigDecimal(3.846).multiply(BigDecimal.valueOf(10.0).pow(33)).multiply(BigDecimal.valueOf(mass).pow(3));
        BigDecimal D = new BigDecimal(1.496 * distance).multiply(BigDecimal.valueOf(10).pow(13));
        BigDecimal T = new BigDecimal(greenhouse * 0.5841);

        BigDecimal tEff = BigDecimal.valueOf(1.0 - albedo).multiply(L).divide(sigma.multiply(BigDecimal.valueOf(Math.PI * 16)), RoundingMode.HALF_UP);

        tEff = sqrt(sqrt(tEff)).divide(sqrt(D), RoundingMode.HALF_UP);

        BigDecimal tEq = tEff.pow(4).multiply(BigDecimal.ONE.add(T.multiply(BigDecimal.valueOf(0.75))));

        tEq = tEq.divide(BigDecimal.valueOf(0.9), ROUND_HALF_UP);

        BigDecimal tKelvin = sqrt(sqrt(tEq));

        temperature = tKelvin.subtract(BigDecimal.valueOf(273.0)).setScale(0, RoundingMode.HALF_UP);

        System.out.println("Temperature is " + temperature.toString());

    }

    @Override
    public String toString() {
        return temperature == null ? "" : temperature.toString() + " \u00b0C";
    }

    private BigDecimal sqrtNewtonRaphson  (BigDecimal c, BigDecimal xn, BigDecimal precision){
        BigDecimal fx = xn.pow(2).add(c.negate());
        BigDecimal fpx = xn.multiply(new BigDecimal(2));
        BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
        xn1 = xn.add(xn1.negate());
        BigDecimal currentSquare = xn1.pow(2);
        BigDecimal currentPrecision = currentSquare.subtract(c);
        currentPrecision = currentPrecision.abs();
        if (currentPrecision.compareTo(precision) <= -1){
            return xn1;
        }
        return sqrtNewtonRaphson(c, xn1, precision);
    }

    private BigDecimal sqrt(BigDecimal c){
        return sqrtNewtonRaphson(c,new BigDecimal(1),new BigDecimal(1).divide(SQRT_PRE));
    }

    public double getMass() {

        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getAlbedo() {
        return albedo;
    }

    public void setAlbedo(double albedo) {
        this.albedo = albedo;
    }

    public double getGreenhouse() {
        return greenhouse;
    }

    public void setGreenhouse(double greenhouse) {
        this.greenhouse = greenhouse;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }
}
