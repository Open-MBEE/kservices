import java.util.*;

import static java.lang.Math.max;


public class TestGenerator {
    static boolean debug = false;
    public static long num(Vector<?> v, boolean b) {
        long ct = 0;
        for ( int i = 0; i < v.size(); ++i ) {
            Object o = v.get(i);
            if ( o instanceof Vector ) {
                ct += num((Vector<?>)o, b);
            } else if (o instanceof Boolean) {
                if ( (Boolean)o == (Boolean)b ) {
                    ct += 1;
                }
            }
        }
        return ct;
    }


    public static boolean allTrue(Vector<?> v) {
        for ( int i = 0; i < v.size(); ++i ) {
            Object o = v.get(i);
            if ( o instanceof Vector ) {
                if (!allTrue((Vector<?>)o)) {
                    return false;
                }
            } else if (o instanceof Boolean) {
                if ( !(Boolean)o ) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean allTrue(boolean a[][]) {
        int s = a.length;
        for ( int i = 0; i < s-1; ++i ) {
            for ( int j = i+1; j < s; ++j ) {
                if (!a[i][j]) return false;
            }
        }
        return true;
    }
    public static Vector<Integer[]> generateByWalking(Vector<Integer> numParamValues, int seed) {
        int s = numParamValues.size();

        Vector<Integer[]> tests = new Vector<>();

        Vector<Vector<Vector<Vector<Boolean>>>> gotPair = new Vector<>();

        // initialize all to false
        for ( int i=0; i < s-1; ++i ) {
            int numParams1 = numParamValues.get(i);
            Vector<Vector<Vector<Boolean>>> v1 = new Vector<>();
            gotPair.add(v1);
            for ( int j=0; j < s; ++j ) {
                int numParams2 = numParamValues.get(j);
                Vector<Vector<Boolean>> v2 = new Vector<>();
                v1.add(v2);
                if ( j <= i ) continue;
                for (int u = 0; u < numParams1; ++u) {
                    Vector<Boolean> v3 = new Vector<>();
                    v2.add(v3);
                    for (int v = 0; v < numParams2; ++v) {
                        v3.add(false);
                    }
                }
            }
        }

//        boolean gotPairr[][] = new boolean[s][s];
//        for ( int i=0; i< s-1; ++i ) {
//            for ( int j=i+1; j < s; ++j ) {
//                gotPairr[i][j] = false;
//            }
//        }
        boolean updatingGotPair = true;

        Integer test[] = new Integer[s];
        for ( int i=0; i < s; ++i ) {
            test[i] = -1;
        }

        Random r = new Random(seed);
        while (!allTrue(gotPair)) {
            //pickRemainingPairs
            for (int i = 0; i < s; ++i) {
                //if (test[i] != -1) continue;
                int numParams1 = numParamValues.get(i);
                int u = r.nextInt(numParams1);
                test[i] = u;
            }
            tests.add(test);
            for (int i = 0; i < s - 1; ++i) {
                for (int j = i + 1; j < s; ++j) {
                    gotPair.get(i).get(j).get(test[i]).set(test[j], true);
                }
            }
            test = new Integer[s];
            for ( int y=0; y<s; ++y ) {
                test[y] = -1;
            }
        }


                    // count based
        while (!allTrue(gotPair)) {
            for ( int i=0; i < s; ++i ) {
                if ( test[i] != -1 ) continue;
                int bestVal = 0;
                long bestNumFalse = 0;
                int numParams1 = numParamValues.get(i);
                for (int u = 0; u < numParams1; ++u) {
                    if (test[i] != -1 && test[i] != u) continue;
                    long numFalse = 0;
                //if (test[i] != -1) continue;
                    for ( int j=0; j < s; ++j ) {
                        if ((j == i || ((test[j] != -1) && (test[i] != -1)))) continue;
                        int numParams2 = numParamValues.get(j);
                        int x = max(i, j);
                        int n = Math.min(i, j);

                    //if (test[i] != -1) break;
                        for (int v = 0; v < numParams2; ++v) {
                            //if (test[i] != -1) break;
                            //if (test[x] != -1 && test[n] != -1) break;
                            if (test[j] != -1 && test[j] != v) continue;
                            Vector<Boolean> bv = gotPair.get(n).get(x).get(u);
                            if (!bv.get(v)) {
                                numFalse += 1;
                            }
                        }
                    }
                    if (numFalse > bestNumFalse) {
                        bestVal = u;
                        bestNumFalse = numFalse;
                    }
                }
                test[i] = bestVal;
                // update gotPair
                for ( int j=0; j < s; ++j ) {
                    if ( test[j] == -1 || i == j ) continue;
                    int x = max(i, j);
                    int n = Math.min(i, j);
                    Vector<Boolean> bv = gotPair.get(n).get(x).get(test[n]);
                    bv.set(test[x], true);
                }
                boolean completeTest = true;
                for ( int x=0; x<s; ++x ) {
                    if ( test[x] == -1 ) {
                        completeTest = false;
                        break;
                    }
                }
                if ( completeTest ) {
                    tests.add(test);
                    test = new Integer[s];
                    for ( int y=0; y<s; ++y ) {
                        test[y] = -1;
                    }
                    break;
                }
            }
        }

        // Greedy
        // assign pairs when a false is found in gotPair
        while (!allTrue(gotPair)) {
            //pickRemainingPairs
            for ( int i=0; i < s-1; ++i ) {
                //if (test[i] != -1) continue;
                int numParams1 = numParamValues.get(i);
//                int bestParam1 = 0;
//                int mostUnmatched1 = 0;
                for ( int j=i+1; j < s; ++j ) {
                    if ( test[j] != -1 && test[i] != -1) continue;
                    //if (test[i] != -1) break;
                    int numParams2 = numParamValues.get(j);
                    if ( j <= i ) continue;
                    for (int u = 0; u < numParams1; ++u) {
                        if ( test[i] != -1 && test[i] != u ) continue;
                        //if (test[i] != -1) break;
                        for (int v = 0; v < numParams2; ++v) {
                            //if (test[i] != -1) break;
                            if ( test[j] != -1 && test[i] != -1 ) break;
                            if ( test[j] != -1 && test[j] != v ) continue;
                            Vector<Boolean> bv = gotPair.get(i).get(j).get(u);
                            if ( !bv.get(v) ) {
                                test[i] = u;
                                test[j] = v;
                                if ( updatingGotPair ) {
                                    bv.set(v, true);
                                }
                                boolean completeTest = true;
                                for ( int x=0; x<s; ++x ) {
                                    if ( test[x] == -1 ) {
                                        completeTest = false;
                                        break;
                                    }
                                }
                                if ( completeTest ) {
                                    tests.add(test);
                                    test = new Integer[s];
                                    for ( int y=0; y<s; ++y ) {
                                        test[y] = -1;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return tests;
    }

    public static long totalPairs(Vector<Integer> numParamValues) {
        long totalPairs = 0;
        int sz = numParamValues.size();
        for ( int i=0; i < sz-1; ++i ) {
            int numParams1 = numParamValues.get(i);
            for ( int j=i+1; j < sz; ++j ) {
                int numParams2 = numParamValues.get(j);
                totalPairs += numParams1 * numParams2;
            }
        }
        return totalPairs;
    }

    public static int minTests(Vector<Integer> numParamValues) {
        int max1 = 0;
        int max2 = 0;
        int sz = numParamValues.size();
        for ( int i=0; i < sz; ++i ) {
            int z = numParamValues.get(i);
            if ( z > max1 ) {
                max2 = max1;
                max1 = z;
            } else if ( z > max2 ) {
                max2 = z;
            }
        }
        return max(max1, max1 * max2);
    }

    protected static Vector<Vector<Vector<Vector<Boolean>>>> _gotPair = new Vector<>();
    public static void zeroGotPairs(Vector<Integer> numParamValues) {
        zeroGotPairs(_gotPair, numParamValues);
    }
    public static void zeroGotPairs(Vector<Vector<Vector<Vector<Boolean>>>> _gotPair,
                                    Vector<Integer> numParamValues) {
        int sz = numParamValues.size();
        if ( _gotPair == null || _gotPair.isEmpty() ) {
            resetGotPairs(numParamValues);
            return;
        }

        // set all to false
        for ( int i=0; i < sz-1; ++i ) {
            int numParams1 = numParamValues.get(i);
            Vector<Vector<Vector<Boolean>>> v1 = _gotPair.get(i);
            for ( int j=i+1; j < sz; ++j ) {
                int numParams2 = numParamValues.get(j);
                Vector<Vector<Boolean>> v2 = v1.get(j);
                for (int u = 0; u < numParams1; ++u) {
                    Vector<Boolean> v3 = v2.get(u);
                    for (int v = 0; v < numParams2; ++v) {
                        v3.set(v,false);
                    }
                }
            }
        }
    }

    public static void resetGotPairs( Vector<Integer> numParamValues ) {
        resetGotPairs(_gotPair, numParamValues);
    }
    public static void resetGotPairs( Vector<Vector<Vector<Vector<Boolean>>>> _gotPair,
                                      Vector<Integer> numParamValues) {
        _gotPair.clear();
        int sz = numParamValues.size();

        long totalPairs = totalPairs(numParamValues);

        // initialize all to false
        for ( int i=0; i < sz-1; ++i ) {
            int numParams1 = numParamValues.get(i);
            Vector<Vector<Vector<Boolean>>> v1 = new Vector<>();
            _gotPair.add(v1);
            for ( int j=0; j < sz; ++j ) {
                int numParams2 = numParamValues.get(j);
                Vector<Vector<Boolean>> v2 = new Vector<>();
                v1.add(v2);
                if ( j <= i ) continue;
                for (int u = 0; u < numParams1; ++u) {
                    Vector<Boolean> v3 = new Vector<>();
                    v2.add(v3);
                    for (int v = 0; v < numParams2; ++v) {
                        v3.add(false);
                    }
                }
            }
        }
    }

    public static boolean putPair(Vector<Vector<Vector<Vector<Boolean>>>> gotPair,
                                  int i, int j, int u, int v ) {
        Vector<Boolean> vv = gotPair.get(i).get(j).get(u);
        if ( vv.get(v) ) {
            return false;
        }
        vv.set(v, true);
        return true;
    }

    public static long numPairs(Vector<Vector<Integer>> tests, Vector<Integer> numParamValues) {
        //int tz = numParamValues.size();
        zeroGotPairs(numParamValues);
        for ( int t=0; t < tests.size(); ++t ) {
            int tz = tests.get(t).size();
            for (int i = 0; i < tz - 1; ++i) {
                Vector<Vector<Vector<Boolean>>> v1 = _gotPair.get(i);
                int u = tests.get(t).get(i);
                for (int j = i + 1; j < tz; ++j) {
                    Vector<Boolean> v2 = v1.get(j).get(u);
                    int v = tests.get(t).get(j);
                    v2.set(v, true);
                }
            }
        }
        return num(_gotPair, true);
    }

    public static int numPairs2(Vector<Vector<Integer>> tests) {
        if ( tests == null || tests.isEmpty() ) {
            return 0;
        }
        Map<Integer, Map<Integer, Map<Integer, Set<Integer>> > > pairs = new LinkedHashMap<>();
        for ( int t=0; t < tests.size(); ++t ) {
            int tz = tests.get(t).size();
            for ( int i = 0; i < tz-1; ++i ) {
                int u = tests.get(t).get(i);
                for ( int j = i+1; j < tz; ++j ) {
                    int v = tests.get(t).get(j);
                    putPair(pairs, i, j, u, v);
                }
            }
        }
        int z = size(pairs);
        return z;
    }

    public static Vector<Vector<Integer>>  generate(Vector<Integer> numParamValues) {
        Vector<Integer[]> best = null;
        for ( int i =1; i < 1000; ++i ) {
            Vector<Integer[]> w = generateByWalking(numParamValues, i);
            if ( best == null || best.size() > w.size() ) {
                best = w;
            }
        }
        // complete search to modify and remove tests
        // iteratively remove the last test case and search for modifications to recapture lost pairs.
        // The search state is the missing pairs and modifications (test #, variable, new value).
        // Use A* - the heuristic is the number of missing pairs.
        int maxTests = best.size();
        int minTests = minTests(numParamValues);
        int numTests = max(minTests, 1 + best.size() / 20);
        long total = totalPairs(numParamValues);
        boolean found = false;
        Vector<Vector<Integer>> optimal = null;
        while ( true ) {
            System.out.println("try max num tests = " + numTests + " within (" + minTests + ", " + maxTests + ")");
            Vector<Vector<Integer>> tests =
                    generateAStar(numParamValues, numTests);
            long numPairs = numPairs(tests, numParamValues);
            if (numPairs == total) {
                optimal = tests;
                if ( tests.size() > minTests ) {
                    maxTests = tests.size();
                    numTests = (maxTests + minTests) / 2;
                } else {
                    break;  // should be equal to minTests
                }
            } else {
                if ( tests.size() < maxTests ) {
                    minTests = numTests + 1;
                    numTests = (maxTests + minTests + 1) / 2;
                } else {
                    break;  // should be equal to maxTests
                }
            }
        }

        return optimal;
    }

    public static boolean putPair(Map<Integer, Map<Integer, Map<Integer, Set<Integer>> > > pairs,
                        int i, int j, int u, int v ) {
        Map<Integer, Map<Integer, Set<Integer>>> first = pairs.get(i);
        if ( first == null ) {
            first = new LinkedHashMap<>();
            pairs.put(i, first);
        }
        return putPair1(first, j, u, v);
    }

    private static boolean putPair1(Map<Integer, Map<Integer, Set<Integer>>> first,
                          int j, int u, int v) {
        Map<Integer, Set<Integer>> second = first.get(j);
        if ( second == null ) {
            second = new LinkedHashMap<>();
            first.put(j, second);
        }
        Set<Integer> s = second.get(u);
        if ( s == null ) {
            s = new TreeSet<>();
            second.put(u, s);
        }
        if ( s.contains(v) ) {
            return false;
        }
        s.add(v);
        return true;
    }

    public static void remove( Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                               Vector<Vector<Vector<Vector<Boolean>>>> fromPairs ) {
        for ( int i : pairs.keySet() ) {
            Map<Integer, Map<Integer, Set<Integer>>> first = pairs.get(i);
            for ( int j : first.keySet() ) {
                Map<Integer, Set<Integer>> second = first.get(j);
                for ( int u : second.keySet() ) {
                    Set<Integer> s = second.get(u);
                    for ( int v : s ) {
                        fromPairs.get(i).get(j).get(u).set(v, false);
                    }
                }
            }
        }
    }

    public static void remove( Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                               Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > fromPairs ) {
        for ( int i : pairs.keySet() ) {
            Map<Integer, Map<Integer, Set<Integer>>> first = pairs.get(i);
            for ( int j : first.keySet() ) {
                Map<Integer, Set<Integer>> second = first.get(j);
                for ( int u : second.keySet() ) {
                    Set<Integer> s = second.get(u);
                    for ( int v : s ) {
                        fromPairs.get(i).get(j).get(u).remove(v);
                    }
                }
            }
        }

    }

    public static int size(Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs) {
        int size = 0;
        for ( Map<Integer, Map<Integer, Set<Integer> > > m1 : pairs.values() ) {
            for ( Map<Integer, Set<Integer> > m2 : m1.values() ) {
                for ( Set<Integer> s : m2.values() ) {
                    size += s.size();
                }
            }
        }
        return size;
    }

    public static class State implements Comparable<State>{
        //Vector<Vector<
        //Map<Integer, Map<Integer, Map<Integer, Integer > > > pairs;
        //Vector<Vector<Vector<Vector<Boolean>>>> gotPair;
        //Vector<Vector<Integer>> tests;
        State prev;
        int var;
        int val;
        //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > newPairs = null;
        int numNewPairs = 0;
        int numPairs;
        int numTests;
        //int testLength;
        //int score = -1;

        @Override
        public String toString() {
            String s = "State@" + this.hashCode() + "(var=" + var + ", val=" + val + ", numNewPairs=" + numNewPairs +
                       ", numPairs=" + numPairs + ", numTests=" + numTests + ")";
            return s;
        }

        public State( State prev, int var, int val,
                      Vector<Vector<Vector<Vector<Boolean>>>> pairs,
                      //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                      Vector<Vector<Integer>> tests, Vector<Integer> numParamValues,
                      int numPairs ) {
            this.prev = prev;
            this.var = var;
            this.val = val;
            this.numNewPairs = update(pairs, tests, numParamValues, numPairs);
            //this.newPairs = update(pairs, tests, numParamValues, numPairs);
            this.numPairs = numPairs + this.numNewPairs;//size(this.newPairs);
            this.numTests = tests.size();

            if (debug) System.out.println("new " + this);
//            if ( tests.size() == 0 ) {
//                this.testLength = 0;
//            } else {
//                Vector<Integer> lastTest = tests.lastElement();
//                this.testLength = (tests.size() - 1) * numParamValues.size() + lastTest.size();
//            }
        }

        //public Map<Integer, Map<Integer, Map<Integer, Set<Integer>>>> update(
        public int update(
                Vector<Vector<Vector<Vector<Boolean>>>> pairs,

                //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                                                                             Vector<Vector<Integer>> tests,
                                                                             Vector<Integer> numParamValues,
                                                                             int numPairs) {
            //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > newPs = new TreeMap<>();
            int numNewPairs = 0;
            Vector<Integer> lastTest = null;
            if ( tests.size() > 0 ) {
                lastTest = tests.lastElement();
            } else {
                lastTest = new Vector<>();
                tests.add(lastTest);
            }
            if ( lastTest.size() == numParamValues.size() ) {
                lastTest = new Vector<Integer>();
                tests.add(lastTest);
            }
            lastTest.add( val );
            int j = lastTest.size()-1;
            int v = val;
            if (debug) System.out.println("putting pairs for last addition to test, " + lastTest + ", in pairs: " + pairs);
            for ( int i = 0; i < j; ++i ) {
                int u = lastTest.get(i);
                boolean newPair = putPair(pairs, i, j, u, v);
                if ( newPair ) {
                    if (debug) System.out.println("new pair (" + i + "=" + u + ", " + j + "=" + v + ")");
                    ++numNewPairs;
                    //putPair(newPs, i, j, u, v);
                    //numPairs += 1;
                }
            }
            if (debug) System.out.println("pairs after updating: " + pairs);
            if (debug) System.out.println("update returning: " + numNewPairs);
            return numNewPairs;//newPs;
        }

        public int backtrack( Vector<Vector<Vector<Vector<Boolean>>>> pairs,
                              //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                              Vector<Vector<Integer>> tests,
                              Vector<Integer> numParamValues ) {
            Vector<Integer> lastTest = tests.lastElement();
            if ( lastTest.size() == 0 ) {
                tests.remove(tests.size()-1);
                lastTest = tests.lastElement();
            }
            int j = lastTest.size()-1;
            int v = val;
            lastTest.remove(j);
            int pairsLost = removeFromPairs(pairs, tests, j, v, lastTest);
            if ( pairsLost != this.numNewPairs ) {
                System.err.println("PRETTY BAD!!!!!!!!!!!!");
            }
//            if ( newPairs != null ) {
//                remove( newPairs, pairs );
//            }
            //this.numPairs -= this.numNewPairs;
            //this.numNewPairs = 0;
            //return this.numPairs - size(this.newPairs);
            int newNumPairs = this.numPairs - this.numNewPairs;
            //this.numNewPairs = 0;
            return newNumPairs;
        }

        private int removeFromPairs(Vector<Vector<Vector<Vector<Boolean>>>> pairs,
                                    Vector<Vector<Integer>> tests,
                                    int j, int v, Vector<Integer> lastTest) {
            if ( j == 0 ) return 0;
            int numPairsLost = 0;
            for (int i = 0; i < lastTest.size(); ++i) {
                // check if pair is in a previous test
                int u = lastTest.get(i);
                boolean match = false;
                for (Vector<Integer> test : tests) {
                    if ( test.size() <= j ) continue;
                    if ( test.get(i) == u && test.get(j) == v ) {
                        match = true;
                        break;
                    }
                }
                if ( !match ) {
                    pairs.get(i).get(j).get(u).set(v, false);
                    numPairsLost += 1;
                }
            }
            return numPairsLost;
        }

        public List<State> expand(
                Vector<Vector<Vector<Vector<Boolean>>>> pairs,
                //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                                  Vector<Vector<Integer>> tests,
                                  Vector<Integer> numParamValues) {
            List<State> newStates = new ArrayList<>();
            Vector<Integer> lastTest = tests.lastElement();
            if ( lastTest.size() == numParamValues.size() ) {
                lastTest = new Vector<Integer>();
                tests.add(lastTest);
            }
            int i = lastTest.size() - 1;
            int j = i + 1;
            for ( int v = 0; v < numParamValues.get(j); ++v ) {
                State s = new State(this, j, v, pairs, tests, numParamValues, numPairs);
                s.backtrack(pairs, tests, numParamValues);
                newStates.add(s);
            }
            return newStates;
        }

        public int score() {
//            if ( score != -1 ) {
//                score = numPairs;// * 4 - testLength;
//            }
//            return score;
            return numPairs;
        }

        @Override
        public int compareTo(State o) {
            if ( this == o ) return 0;
            if ( o == null ) return -1;
            int score = score();
            int oscore = o.score();
            if ( score > oscore ) return -1;
            if ( score < oscore ) return 1;
            if ( numTests < o.numTests ) return -1;
            if ( numTests > o.numTests ) return 1;
            if ( var > o.var ) return -1;
            if ( var < o.var ) return 1;
            if ( val < o.val ) return -1;
            if ( val > o.val ) return 1;
            if ( prev == o.prev ) return 0;
            if ( prev == null ) return 1;
            if ( o.prev == null ) return -1;
            return -1 * Integer.compare(prev.hashCode(), o.prev.hashCode());
        }
    }

    public static int update( State parent, State toState,
                              Vector<Vector<Vector<Vector<Boolean>>>> pairs,
                              //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                              Vector<Vector<Integer>> tests,
                              Vector<Integer> numParamValues ) {
        if (debug) System.out.println("BEGIN update(parent=" + parent + ", toState=" + toState + ")");
        if ( toState == parent ) return toState.numPairs;
        int numPairs = parent.numPairs;
        if ( toState.prev != parent ) {
            numPairs = update(parent, toState.prev, pairs, tests, numParamValues);
        }
        //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > newPs =
        if (debug) System.out.println("before update: " + toState);
        int numNewPairs =
                toState.update(pairs, tests, numParamValues, numPairs);
        if (debug) System.out.println("after update:  " + toState);
        if (debug) System.out.println("numNewPairs:  " + numNewPairs);
//        if ( newPs != null ) {
//            numPairs += size(newPs);
//        }
        numPairs += numNewPairs;
        if (debug) System.out.println("numPairs:  " + numPairs);
        if ( numPairs != toState.numPairs ) {
            System.err.println("BAD!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        if (debug) System.out.println("END update(parent=" + parent + ", toState=" + toState + ")");
        return numPairs;
    }

    public static void rejigger( State fromState, State toState,
                                 Vector<Vector<Vector<Vector<Boolean>>>> pairs,
                                 //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                                 Vector<Vector<Integer>> tests,
                                 Vector<Integer> numParamValues ) {
        if (debug) System.out.println("rejigger(fromState=" + fromState + ", toState=" + toState + ")");
        State p = commonParent(fromState, toState, new TreeSet<>());
        State pf = fromState;
        int numPairs = pf.numPairs;
        while ( pf != p ) {
            if (debug) System.out.println("before numPairs:  " + numPairs);
            if (debug) System.out.println("before backtrack: " + pf);
            numPairs = pf.backtrack(pairs, tests, numParamValues);
            if (debug) System.out.println("after backtrack:  " + pf);
            if (debug) System.out.println("after numPairs:  " + numPairs);
            pf = pf.prev;
        }
        update(p, toState, pairs, tests, numParamValues);
    }

    public static State commonParent(State fromState, State toState, Set<State> visited) {
        if ( fromState == toState ) return fromState;
        if ( fromState != null && visited.contains(fromState) ) {
            return fromState;
        }
        if ( toState != null && visited.contains(toState) ) {
            return toState;
        }
        State pf = null;
        State pt = null;
        if ( fromState != null ) {
            visited.add(fromState);
            pf = fromState.prev;
        }
        if ( toState != null ) {
            visited.add(toState);
            pt = toState.prev;
        }
        return commonParent(pf, pt, visited);
    }

    public static State initialState(Vector<Vector<Vector<Vector<Boolean>>>> pairs,
                                     //Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                                     Vector<Vector<Integer>> tests,
                                     Vector<Integer> numParamValues ) {
        State s = null;
        int minVals = 0;
        int sz = numParamValues.size();
        for (int i = 0; i < sz; ++i) {
            if ( minVals == 0 || numParamValues.get(i) < minVals ) {
                minVals = numParamValues.get(i);
            }
        }
        int numPairs = 0;

        minVals = 1; // HACK -- TODO -- FIXME -- Can't assume more than this.  See below.
        // for [2,2,2], can't find solution with 000 and 111
        // 000
        // 111
        // 011
        // 100

        for ( int j = 0; j < minVals; ++j ) {
            for (int i = 0; i < sz; ++i) {
                s = new State(s, i, j, pairs, tests, numParamValues, numPairs);
                numPairs = s.numPairs;
            }
        }
        return s;
    }

    public static Vector<Vector<Integer>> generateAStar(Vector<Integer> numParamValues,
                                                        int numTestsUpperbound) {
        //Vector<Integer[]> tests = new Vector<>();
        resetGotPairs(numParamValues);
        Vector<Vector<Integer>> tests = new Vector<>();

        Vector<Vector<Vector<Vector<Boolean>>>> gotPair = new Vector<>();
        resetGotPairs(gotPair, numParamValues);

        int sz = numParamValues.size();

        long totalPairs = totalPairs(numParamValues);
        System.out.println("total pairs = " + totalPairs);

//        Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs =
//                new LinkedHashMap<>();
        Vector<Vector<Vector<Vector<Boolean>>>> pairs = gotPair;
        int numPairs = 0;

        State s = initialState(pairs, tests, numParamValues);
        TreeSet<State> q = new TreeSet<>();
        q.add(s);
        State best = s;
        int bestNumPairs = s.numPairs;
        int ct = 0;
        while ( !q.isEmpty() ) {
            State lastState = s;
            s = q.first();
            q.remove(s);
            ++ct;
            rejigger(lastState, s, pairs, tests, numParamValues);

            if ( s.numPairs == totalPairs ) {
                best = s;
                break;
            }
            if ( s.numTests > numTestsUpperbound ) {
                continue;
            }
            if ( s.numPairs > best.numPairs ) {
                best = s;
            }
            List<State> newStates = s.expand(pairs, tests, numParamValues);
            q.addAll(newStates);
        }
        rejigger(s, best, pairs, tests, numParamValues);
        System.out.println("numPairs = " + best.numPairs + " out of " + totalPairs);
        return tests;
    }

    public static void main(String args[]) {
        for ( int numVars = 2; numVars <= 5; ++numVars ) {
            for (int numVals = 2; numVals <= 5; ++numVals) {
                Vector<Integer> v = new Vector<>();
                for (int i = 0; i < numVars; ++i) {
                    v.add(numVals);
                }
                System.out.println(v);
                Vector<Vector<Integer>> vv = generate(v);
                System.out.println("final number of tests: " + (vv == null ? "null" : ("" + vv.size())));
            }
        }
    }
}
