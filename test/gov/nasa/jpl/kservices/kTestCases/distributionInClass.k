import gov.nasa.jpl.ae.util.distributions.Normal

//ParameterListenerImpl.setMaxLoopsWithNoProgress(300)

class A {
    var mu : Integer
    var sigma : Real
    req sigma > 0.0
    req sigma <= 2.0
    y : Normal = Normal(mu,sigma)
}
a:A
