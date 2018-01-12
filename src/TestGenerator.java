import java.util.*;

import static java.lang.Math.max;

public class TestGenerator {
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

    public static int totalPairs(Vector<Integer> numParamValues) {
        int totalPairs = 0;
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

    public static int numPairs(Vector<Vector<Integer>> tests) {
        if ( tests == null || tests.isEmpty() ) {
            return 0;
        }
        Map<Integer, Map<Integer, Map<Integer, Set<Integer>> > > pairs = new HashMap<>();
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
        int total = totalPairs(numParamValues);
        boolean found = false;
        Vector<Vector<Integer>> optimal = null;
        while ( true ) {
            System.out.println("try max num tests = " + numTests + " within (" + minTests + ", " + maxTests + ")");
            Vector<Vector<Integer>> tests =
                    generateAStar(numParamValues, numTests);
            int numPairs = numPairs(tests);
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
            first = new HashMap<>();
            pairs.put(i, first);
        }
        return putPair1(first, j, u, v);
    }

    private static boolean putPair1(Map<Integer, Map<Integer, Set<Integer>>> first,
                          int j, int u, int v) {
        Map<Integer, Set<Integer>> second = first.get(j);
        if ( second == null ) {
            second = new HashMap<>();
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
        Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > newPairs = null;
        int numPairs;
        int numTests;

        public State( State prev, int var, int val,
                      Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                      Vector<Vector<Integer>> tests, Vector<Integer> numParamValues,
                      int numPairs ) {
            this.prev = prev;
            this.var = var;
            this.val = val;
            this.newPairs = update(pairs, tests, numParamValues, numPairs);
            this.numPairs = numPairs + size(this.newPairs);
            this.numTests = tests.size();
        }

        public Map<Integer, Map<Integer, Map<Integer, Set<Integer>>>> update(Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                                                                             Vector<Vector<Integer>> tests,
                                                                             Vector<Integer> numParamValues,
                                                                             int numPairs) {
            Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > newPs = new TreeMap<>();
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
            for ( int i = 0; i < j; ++i ) {
                int u = lastTest.get(i);
                boolean newPair = putPair(pairs, i, j, u, v);
                if ( newPair ) {
                    putPair(newPs, i, j, u, v);
                    numPairs += 1;
                }
            }
            return newPs;
        }

        public int backtrack(Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
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
            if ( newPairs != null ) {
                remove( newPairs, pairs );
            }
            return this.numPairs - size(this.newPairs);
        }

        public List<State> expand(Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
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
            return numPairs;// - numTests;
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
                              Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                              Vector<Vector<Integer>> tests,
                              Vector<Integer> numParamValues ) {
        if ( toState == parent ) return toState.numPairs;
        int numPairs = parent.numPairs;
        if ( toState.prev != parent ) {
            numPairs = update(parent, toState.prev, pairs, tests, numParamValues);
        }
        Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > newPs =
                toState.update(pairs, tests, numParamValues, numPairs);
        if ( newPs != null ) {
            numPairs += size(newPs);
        }
        if ( numPairs != toState.numPairs ) {
            System.err.println("BAD!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        return numPairs;
    }

    public static void rejigger( State fromState, State toState,
                                 Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
                                 Vector<Vector<Integer>> tests,
                                 Vector<Integer> numParamValues ) {
        State p = commonParent(fromState, toState, new TreeSet<>());
        State pf = fromState;
        int numPairs = pf.numPairs;
        while ( pf != p ) {
            numPairs = pf.backtrack(pairs, tests, numParamValues);
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

    public static State initialState(Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs,
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
        Vector<Vector<Integer>> tests = new Vector<>();

        Vector<Vector<Vector<Vector<Boolean>>>> gotPair = new Vector<>();

        int sz = numParamValues.size();

        int totalPairs = totalPairs(numParamValues);
        System.out.println("total pairs = " + totalPairs);

        // initialize all to false
        for ( int i=0; i < sz-1; ++i ) {
            int numParams1 = numParamValues.get(i);
            Vector<Vector<Vector<Boolean>>> v1 = new Vector<>();
            gotPair.add(v1);
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


        Map<Integer, Map<Integer, Map<Integer, Set<Integer> > > > pairs =
                new HashMap<>();
        int numPairs = 0;

        State s = initialState(pairs, tests, numParamValues);
        TreeSet<State> q = new TreeSet<>();
        q.add(s);
        State best = s;
        int bestNumPairs = s.numPairs;
        while ( !q.isEmpty() ) {
            State lastState = s;
            s = q.first();
            q.remove(s);
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
