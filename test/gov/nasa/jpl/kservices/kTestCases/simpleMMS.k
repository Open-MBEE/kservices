
class A {
    x:Real = 2.71
    y:Real
}
a:A

MMSConnect.instance.connect(a.x, "t_s", false)
MMSConnect.instance.connect(a.y, "case100_t8_ub", true)
