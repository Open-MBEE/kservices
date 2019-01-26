import gov.nasa.jpl.ae.util.distributions.Normal

class A {
    var mu : Integer
    var sigma : Real
    req sigma > 0.0
    req sigma <= 2.0
    y : Normal = Normal(mu,sigma)
}
a:A
