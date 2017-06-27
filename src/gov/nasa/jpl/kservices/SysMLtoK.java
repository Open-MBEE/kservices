package gov.nasa.jpl.kservices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nasa.jpl.mbee.util.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class SysMLtoK {
  // Constant names, as used in kheader
  protected static final String S2K_EVENT = "S2K_Event";
  protected static final String S2K_STATE = "S2K_State";
  protected static final String S2K_TRANSITION = "transition";

  // Connect element types to the appropriate translator code:
  protected static final HashMap<String,Translator> translations = makeTranslations();
  private static HashMap<String,Translator> makeTranslations() {
    // As is done below, all element types should be in alphabetical order.
    HashMap<String,Translator> t = new HashMap<String,Translator>();
    t.put("AcceptEventAction"           , new AcceptEventActionTr());
    t.put("Activity"                    , new ActivityTr());
    t.put("ActivityFinalNode"           , new ActivityFinalNodeTr());
    t.put("ActivityParameterNode"       , new ActivityParameterNodeTr());
    t.put("Association"                 , new AssociationTr());
    t.put("AssociationClass"            , new AssociationClassTr());
    t.put("CallBehaviorAction"          , new CallBehaviorActionTr());
    t.put("ChangeEvent"                 , new ChangeEventTr());
    t.put("Class"                       , new ClassTr());
    t.put("Comment"                     , new CommentTr());
    t.put("Connector"                   , new ConnectorTr());
    t.put("ConnectorEnd"                , new ConnectorEndTr());
    t.put("Constraint"                  , new ConstraintTr());
    t.put("ControlFlow"                 , new ControlFlowTr());
    t.put("DecisionNode"                , new DecisionNodeTr());
    t.put("Dependency"                  , new DependencyTr());
    t.put("Diagram"                     , new DiagramTr());
    t.put("Duration"                    , new DurationTr());
    t.put("DurationConstraint"          , new DurationConstraintTr());
    t.put("DurationInterval"            , new DurationIntervalTr());
    t.put("DurationObservation"         , new DurationObservationTr());
    t.put("ElementValue"                , new ElementValueTr());
    t.put("Expression"                  , new ExpressionTr());
    t.put("FinalState"                  , new FinalStateTr());
    t.put("ForkNode"                    , new ForkNodeTr());
    t.put("Generalization"              , new GeneralizationTr());
    t.put("InitialNode"                 , new InitialNodeTr());
    t.put("InputPin"                    , new InputPinTr());
    t.put("InstanceSpecification"       , new InstanceSpecificationTr());
    t.put("InstanceValue"               , new InstanceValueTr());
    t.put("InterruptibleActivityRegion" , new InterruptibleActivityRegionTr());
    t.put("JoinNode"                    , new JoinNodeTr());
    t.put("LiteralBoolean"              , new LiteralBooleanTr());
    t.put("LiteralInteger"              , new LiteralIntegerTr());
    t.put("LiteralReal"                 , new LiteralRealTr());
    t.put("LiteralString"               , new LiteralStringTr());
    t.put("LiteralUnlimitedNatural"     , new LiteralUnlimitedNaturalTr());
    t.put("MergeNode"                   , new MergeNodeTr());
    t.put("Model"                       , new ModelTr());
    t.put("Mount"                       , new MountTr());
    t.put("ObjectFlow"                  , new ObjectFlowTr());
    t.put("OpaqueAction"                , new OpaqueActionTr());
    t.put("OpaqueExpression"            , new OpaqueExpressionTr());
    t.put("Operation"                   , new OperationTr());
    t.put("OutputPin"                   , new OutputPinTr());
    t.put("Package"                     , new PackageTr());
    t.put("Package"                     , new PackageTr());
    t.put("Parameter"                   , new ParameterTr());
    t.put("Port"                        , new PortTr());
    t.put("ProfileApplication"          , new ProfileApplicationTr());
    t.put("Project"                     , new ProjectTr());
    t.put("Property"                    , new PropertyTr());
    t.put("Pseudostate"                 , new PseudostateTr());
    t.put("Region"                      , new RegionTr());
    t.put("SendSignalAction"            , new SendSignalActionTr());
    t.put("Signal"                      , new SignalTr());
    t.put("SignalEvent"                 , new SignalEventTr());
    t.put("Slot"                        , new SlotTr());
    t.put("State"                       , new StateTr());
    t.put("StateMachine"                , new StateMachineTr());
    t.put("TimeEvent"                   , new TimeEventTr());
    t.put("TimeExpression"              , new TimeExpressionTr());
    t.put("Transition"                  , new TransitionTr());
    t.put("Trigger"                     , new TriggerTr());
    return t;
  }
  
  /**
   * Mapping from sysmlid -> element translation in K
   */
  public static class TranslationMap extends HashMap<String,Interpolator>{
    private String rootId;
    
    public TranslationMap() {
      super();
    }
    
    public String getRootId() {
      return rootId;
    }
    public void setRootId(String rootId) throws S2KException {
      if (!this.containsKey(rootId)) {
        throw new S2KException("rootId must be a key in the TranslationMap");
      }
      this.rootId = rootId;
    }
    
    public Interpolator[] getAll(String[] keys) {
      Interpolator[] output = new Interpolator[keys.length];
      for (int i = 0; i < keys.length; i++) {
        output[i] = this.get(keys[i]);
      }
      return output;
    }
  };
  
  /**
   * For translation of sysml elements to K fragments.
   */
  protected interface Translator {
    /**
     * Perform initial element translation, generating a fragment.
     * @param jsonObj The element to translate.
     * @throws JSONException if element 
     */
    public Interpolator translate(JSONObject jsonObj) throws S2KException;
  }
  
  /**
   * For interpolation of translated fragments.
   */
  protected interface Interpolator {
    /**
     * Perform the interpolation and return the result.
     * @param tm TranslationMap for all fragments
     * @return Resultant code
     */
    public String interpolate(TranslationMap tm) throws S2KException;
  }
  
  /**
   * Simple interpolator for elements with no dependencies.
   */
  protected static class ConstantIp implements Interpolator {
    private String output;
    public ConstantIp(String myOutput) {
      output = myOutput;
    }
    public String interpolate(TranslationMap tm) {
      return output;
    }
  }
  
  /**
   * Convenience class, for constant formatted output.
   */
  protected static class FormatIp extends ConstantIp {
    public FormatIp(String format, Object... args) {
      super(String.format(format, args));
    }
  }
  
  /**
   * Interpolator that can expose their reference name in the K code.
   */
  protected interface ReferenceInterpolator extends Interpolator {
    public String reference(TranslationMap tm);
  }
  
  /// Public Interface
  
  /**
   * Reads JSON object from a file.
   * @param filePath Any acceptable path to JSON file.
   * @return JSONObject representing contents of file.
   * @throws S2KParseException if file is not valid JSON.
   * @throws FileNotFoundException
   */
  public static JSONObject readJSONFile(String filePath) throws S2KParseException, FileNotFoundException {
    try {
      return new JSONObject( FileUtils.fileToString(filePath) );
    } catch (JSONException e) {
      throw new S2KParseException("Invalid JSON structure.", e);
    }
  }
  
  /**
   * Translates all the elements of a project individually.
   * 
   * @param jsonObj The project JSON object, which must have an "elements" attribute.
   * @return TranslationMap of sysmlid's to K translations
   * @throws S2KParseException if JSON does not contain required attributes.
   * @throws S2KException if other errors occur
   */
  public static TranslationMap translateElements(JSONObject jsonObj) throws S2KException {
    try {
      TranslationMap translationMap = new TranslationMap();
      JSONArray      jsonElements   = jsonObj.getJSONArray("elements");
      JSONObject     element;
      for (int i = 0; i < jsonElements.length(); ++i) {
        element = jsonElements.getJSONObject(i);
        translationMap.put(getId(element), translateElement(element));
      }
      return translationMap;
    } catch (JSONException e) {
      throw new S2KParseException("Unsupported SysML JSON structure.",e);
    }
  }

  /**
   * Translates a single element.
   * @param element JSONObject for the sysml element to be translated
   * @return Interpolator for the given element
   * @throws S2KParseException if element is missing a necessary attribute
   * @throws S2KException if other errors occur
   */
  protected static Interpolator translateElement(JSONObject element) throws S2KException {
    try {
      Translator elTrans = translations.get(element.getString("type"));
      if (elTrans == null) {
        throw new S2KParseException("Element type not supported.");
      }
      return elTrans.translate(element);
    } catch (JSONException e) {
      throw new S2KParseException("Unsupported SysML JSON structure.",e);
    }
  }
  
  /**
   * Generates a full K source program for a given model.
   * @param elementMap The map of sysmlid's to interpolators built by translateElements
   * @return The corresponding K program.
   * @throws S2KException if kheader isn't found.
   */
  public static String generateKSource(TranslationMap elementMap) throws S2KException {
    try (Scanner s = new Scanner( SysMLtoK.class.getResourceAsStream("/kheader.txt") )) {
      String output = s.useDelimiter("\\Z").next();
      
      for (Map.Entry<String, Interpolator> en : elementMap.entrySet()) {
        System.out.printf("DEBUG: %s  --  %s%n", en.getValue().interpolate(elementMap), en.getKey()); //DEBUG
      }
      
      return output;
    }
  }
  
  /// Private helpers
  
  private static String getId(JSONObject element) throws JSONException {
    String id = element.optString("sysmlid");
    if (id == null || id.equals("")) id = element.getString("id");
    return id;
  }
  
  private static String getReference(JSONObject element) throws JSONException {
    String ref = element.optString("name");
    if (ref == null || ref.equals("")) ref = getId(element);
    return ref;
  }
  
  private static String[] jsonArrToStringArr(JSONArray json) throws S2KParseException {
    String[] output = new String[json.length()];
    for (int i = 0; i < output.length; i++) {
      output[i] = json.getString(i);
    }
    return output;
  }
  
  private static ReferenceInterpolator makeRefIp(Interpolator ip) throws S2KException {
    try {
      return (ReferenceInterpolator) ip;
    } catch (ClassCastException e) {
      throw new S2KException("Interpolator was not a reference interpolator.", e);
    }
  }
  
  private static String indent(String s) {
    return indent(s, "  ");
  }
  
  private static String indent(String s, String space) {
    return space + s.replace("\n", "\n" + space);
  }
  
  // Define each translation individually:
  // For brevity and clarity, all translators should be named "sysml element type" + "Tr"
  // For consistency, all translators should be added to this file in alphabetical order
  // TODO: decide on placeholder format, and implement in every method

  protected static class AcceptEventActionTr implements Translator {
    protected class AcceptEventActionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- AcceptEventAction";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement AcceptEventActionTr.translate
      return new AcceptEventActionIp();
    }
  }

  protected static class ActivityTr implements Translator {
    protected class ActivityIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Activity";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ActivityTr.translate
      return new ActivityIp();
    }
  }

  protected static class ActivityFinalNodeTr implements Translator {
    protected class ActivityFinalNodeIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ActivityFinalNode";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ActivityFinalNodeTr.translate
      return new ActivityFinalNodeIp();
    }
  }

  protected static class ActivityParameterNodeTr implements Translator {
    protected class ActivityParameterNodeIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ActivityParameterNode";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ActivityParameterNodeTr.translate
      return new ActivityParameterNodeIp();
    }
  }

  protected static class AssociationTr implements Translator {
    protected class AssociationIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Association";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement AssociationTr.translate
      return new AssociationIp();
    }
  }

  protected static class AssociationClassTr implements Translator {
    protected class AssociationClassIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- AssociationClass";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement AssociationClassTr.translate
      return new AssociationClassIp();
    }
  }

  protected static class CallBehaviorActionTr implements Translator {
    protected class CallBehaviorActionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- CallBehaviorAction";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement CallBehaviorActionTr.translate
      return new CallBehaviorActionIp();
    }
  }

  protected static class ChangeEventTr implements Translator {
    protected class ChangeEventIp implements ReferenceInterpolator {
      private String referenceName;
      public ChangeEventIp(String name) {
        referenceName = name;
      }
      public String interpolate(TranslationMap tm) {
        return String.format(
            "class %1$s extends %2$s",
            referenceName, S2K_EVENT);
      }
      public String reference(TranslationMap tm) {
        return referenceName;
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      return new ChangeEventIp(getReference(jsonObj));
    }
  }

  protected static class ClassTr implements Translator {
    protected class ClassIp implements Interpolator {
      private String name;
      private Interpolator contentIp;
      public ClassIp(String myName, JSONObject contentJson) throws S2KException {
        name      = myName;
        contentIp = (contentJson == null ? null : translateElement(contentJson));
      }
      public String interpolate(TranslationMap tm) throws S2KException {
        // TODO: figure out what needs to be called for interpolation
        if (contentIp == null) {
          return String.format("class %1$s", name);
        } else {
          return String.format(
              "class %1$s {%n"
            + "  %2$s%n"
            + "}",
              name,
              contentIp.interpolate(tm));
        }
      }
    }
    public Interpolator translate(JSONObject jsonObj) throws S2KException, JSONException {
      return new ClassIp(getReference(jsonObj), jsonObj.optJSONObject("_contents"));
    }
  }

  protected static class CommentTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      return new FormatIp("-- %s",jsonObj.getString("body"));
    }
  }

  protected static class ConnectorTr implements Translator {
    protected class ConnectorIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Connector";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ConnectorTr.translate
      return new ConnectorIp();
    }
  }

  protected static class ConnectorEndTr implements Translator {
    protected class ConnectorEndIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ConnectorEnd";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ConnectorEndTr.translate
      return new ConnectorEndIp();
    }
  }

  protected static class ConstraintTr implements Translator {
    protected class ConstraintIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Constraint";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ConstraintTr.translate
      return new ConstraintIp();
    }
  }

  protected static class ControlFlowTr implements Translator {
    protected class ControlFlowIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ControlFlow";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ControlFlowTr.translate
      return new ControlFlowIp();
    }
  }

  protected static class DecisionNodeTr implements Translator {
    protected class DecisionNodeIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- DecisionNode";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement DecisionNodeTr.translate
      return new DecisionNodeIp();
    }
  }

  protected static class DependencyTr implements Translator {
    protected class DependencyIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Dependency";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement DependencyTr.translate
      return new DependencyIp();
    }
  }

  protected static class DiagramTr implements Translator {
    protected class DiagramIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Diagram";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement DiagramTr.translate
      return new DiagramIp();
    }
  }

  protected static class DurationTr implements Translator {
    protected class DurationIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Duration";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement DurationTr.translate
      return new DurationIp();
    }
  }

  protected static class DurationConstraintTr implements Translator {
    protected class DurationConstraintIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- DurationConstraint";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement DurationConstraintTr.translate
      return new DurationConstraintIp();
    }
  }

  protected static class DurationIntervalTr implements Translator {
    protected class DurationIntervalIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- DurationInterval";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement DurationIntervalTr.translate
      return new DurationIntervalIp();
    }
  }

  protected static class DurationObservationTr implements Translator {
    protected class DurationObservationIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- DurationObservation";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement DurationObservationTr.translate
      return new DurationObservationIp();
    }
  }

  protected static class ElementValueTr implements Translator {
    protected class ElementValueIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ElementValue";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ElementValueTr.translate
      return new ElementValueIp();
    }
  }

  protected static class ExpressionTr implements Translator {
    protected class ExpressionIp implements Interpolator {
      private Interpolator[] operandIps;
      public ExpressionIp(JSONArray operands) throws S2KException {
        operandIps = new Interpolator[operands.length()];
        for (int i = 0; i < operandIps.length; ++i) {
          operandIps[i] = translateElement(operands.getJSONObject(i));
        }
      }
      public String interpolate(TranslationMap tm) throws S2KException {
        // TODO: actually parse the expression, once operators/operand structure is understood
        String output = "(";
        for (Interpolator opIp : operandIps) {
          output += opIp.interpolate(tm) + ",";
        }
        return output + ")";
      }
    }
    public Interpolator translate(JSONObject jsonObj) throws S2KException {
      // TODO: Implement ExpressionTr.translate
      return new ExpressionIp(jsonObj.getJSONArray("operand"));
    }
  }

  protected static class FinalStateTr implements Translator {
    protected class FinalStateIp implements ReferenceInterpolator {
      private String name;
      public FinalStateIp(String myName) {
        name = myName;
      }
      public String interpolate(TranslationMap tm) {
        return String.format(
            "%1$s : %2$s",
            name, S2K_STATE);
      }
      public String reference(TranslationMap tm) {
        return name;
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      return new FinalStateIp(getReference(jsonObj));
    }
  }

  protected static class ForkNodeTr implements Translator {
    protected class ForkNodeIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ForkNode";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ForkNodeTr.translate
      return new ForkNodeIp();
    }
  }

  protected static class GeneralizationTr implements Translator {
    protected class GeneralizationIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Generalization";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement GeneralizationTr.translate
      return new GeneralizationIp();
    }
  }

  protected static class InitialNodeTr implements Translator {
    protected class InitialNodeIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- InitialNode";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement InitialNodeTr.translate
      return new InitialNodeIp();
    }
  }

  protected static class InputPinTr implements Translator {
    protected class InputPinIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- InputPin";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement InputPinTr.translate
      return new InputPinIp();
    }
  }

  protected static class InstanceSpecificationTr implements Translator {
    protected class InstanceSpecificationIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- InstanceSpecification";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement InstanceSpecificationTr.translate
      return new InstanceSpecificationIp();
    }
  }

  protected static class InstanceValueTr implements Translator {
    protected class InstanceValueIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- InstanceValue";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement InstanceValueTr.translate
      return new InstanceValueIp();
    }
  }

  protected static class InterruptibleActivityRegionTr implements Translator {
    protected class InterruptibleActivityRegionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- InterruptibleActivityRegion";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement InterruptibleActivityRegionTr.translate
      return new InterruptibleActivityRegionIp();
    }
  }

  protected static class JoinNodeTr implements Translator {
    protected class JoinNodeIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- JoinNode";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement JoinNodeTr.translate
      return new JoinNodeIp();
    }
  }

  protected static class LiteralBooleanTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      return new ConstantIp(jsonObj.getBoolean("value") ? "true" : "false");
    }
  }

  protected static class LiteralIntegerTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      // Explicitly check that the value is integral
      return new FormatIp("%d", jsonObj.getInt("value"));
    }
  }

  protected static class LiteralRealTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      return new FormatIp("%f", jsonObj.getDouble("value"));
    }
  }

  protected static class LiteralStringTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: think about adding some kind of "K-escaping" to this string, or to the translation more generally
      return new FormatIp("\"%s\"", jsonObj.getString("value"));
    }
  }

  protected static class LiteralUnlimitedNaturalTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Think about adding a "SysML Prolog" in K to every translation to better accomodate things like this...
      return new FormatIp("%d", jsonObj.getInt("value"));
    }
  }

  protected static class MergeNodeTr implements Translator {
    protected class MergeNodeIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- MergeNode";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement MergeNodeTr.translate
      return new MergeNodeIp();
    }
  }

  protected static class ModelTr implements Translator {
    protected class ModelIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Model";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ModelTr.translate
      return new ModelIp();
    }
  }

  protected static class MountTr implements Translator {
    protected class MountIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Mount";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement MountTr.translate
      return new MountIp();
    }
  }

  protected static class ObjectFlowTr implements Translator {
    protected class ObjectFlowIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ObjectFlow";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ObjectFlowTr.translate
      return new ObjectFlowIp();
    }
  }

  protected static class OpaqueActionTr implements Translator {
    protected class OpaqueActionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- OpaqueAction";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement OpaqueActionTr.translate
      return new OpaqueActionIp();
    }
  }

  protected static class OpaqueExpressionTr implements Translator {
    protected class OpaqueExpressionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- OpaqueExpression";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement OpaqueExpressionTr.translate
      return new OpaqueExpressionIp();
    }
  }

  protected static class OperationTr implements Translator {
    protected class OperationIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Operation";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement OperationTr.translate
      return new OperationIp();
    }
  }

  protected static class OutputPinTr implements Translator {
    protected class OutputPinIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- OutputPin";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement OutputPinTr.translate
      return new OutputPinIp();
    }
  }

  protected static class PackageTr implements Translator {
    protected class PackageIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Package";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement PackageTr.translate
      return new PackageIp();
    }
  }

  protected static class ParameterTr implements Translator {
    protected class ParameterIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Parameter";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ParameterTr.translate
      return new ParameterIp();
    }
  }

  protected static class PortTr implements Translator {
    protected class PortIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Port";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement PortTr.translate
      return new PortIp();
    }
  }

  protected static class ProfileApplicationTr implements Translator {
    protected class ProfileApplicationIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ProfileApplication";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ProfileApplicationTr.translate
      return new ProfileApplicationIp();
    }
  }

  protected static class ProjectTr implements Translator {
    protected class ProjectIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Project";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ProjectTr.translate
      return new ProjectIp();
    }
  }

  protected static class PropertyTr implements Translator {
    public class PropertyIp implements Interpolator {
      private String name;
      private Interpolator defaultValueIp;
      private String typeName;
      public PropertyIp(String myName, JSONObject defaultValue, String typeId) throws S2KException {
        name = myName;
        // TODO: translate the typeId into a meaningful type name
        if (defaultValue == null) {
          if (typeId == null) {
            throw new S2KParseException("Property has no type.");
          } else {
            typeName = typeId;
          }
          defaultValueIp = null;
        } else {
          typeName = (typeId == null ? defaultValue.getString("typeId") : typeId);
          defaultValueIp = translateElement(defaultValue);
        }
      }
      public String interpolate(TranslationMap tm) throws S2KException {
        return String.format(
            "%1$s : %2$s",
            name, typeName
          ) + (defaultValueIp == null ? "" : " = " + defaultValueIp.interpolate(tm));
      }
    }
    public Interpolator translate(JSONObject jsonObj) throws S2KException {
      return new PropertyIp(
          getReference(jsonObj),
          jsonObj.optJSONObject("defaultValue"),
          jsonObj.optString("typeId"));
    }
  }

  protected static class PseudostateTr implements Translator {
    protected class PseudostateIp implements ReferenceInterpolator {
      private String name;
      public PseudostateIp(String myName) {
        name = myName;
      }
      public String interpolate(TranslationMap tm) {
        return String.format(
            "%1$s : %2$s",
            name, S2K_STATE);
      }
      public String reference(TranslationMap tm) {
        return name;
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      return new PseudostateIp(getReference(jsonObj));
    }
  }

  protected static class RegionTr implements Translator {
    protected class RegionIp implements Interpolator {
      private String   stateMachineId;
      private String[] subvertexIds;
      private String[] transitionIds;
      
      public RegionIp(String myStateMachineId, String[] mySubvertexIds, String[] myTransitionIds) {
        stateMachineId = myStateMachineId;
        subvertexIds   = mySubvertexIds;
        transitionIds  = myTransitionIds;
      }
      public String interpolate(TranslationMap tm) throws S2KException {
        ReferenceInterpolator smIp = makeRefIp(tm.get(stateMachineId));
        if (smIp == null) {
          System.out.printf("DEBUG: stateMachineId: %s%n", stateMachineId); //DEBUG
          throw new S2KException("StateMachine was referenced but not defined");
        }
        
        Interpolator[] svIps = tm.getAll(subvertexIds), trIps = tm.getAll(transitionIds);
        
        String svStr = "", trStr = "";
        for (Interpolator svIp : svIps) {
          if (svIp == null) throw new S2KException("Subvertex was referenced but not defined.");
          svStr += String.format("%1$s%n", indent(svIp.interpolate(tm)));
        }
        for (Interpolator trIp : trIps) {
          if (trIp == null) throw new S2KException("Transition was referenced but not defined.");
          trStr += String.format("%s%n", indent(trIp.interpolate(tm)));
        }
        
        return String.format(
            "class %1$s {%n"
          + "  -- Events:%n"
          + "%2$s%n"
          + "  %n"
          + "  -- States:%n"
          + "%3$s%n"
          + "  %n"
          + "  -- Transitions:%n"
          + "  fun %7$s(s : %5$s, e : %6$s) : %5$s%n"
          + "%4$s%n"
          + "  %n"
          + "}",
            smIp.reference(tm),
            smIp.interpolate(tm),
            svStr,
            trStr,
            S2K_STATE,
            S2K_EVENT,
            S2K_TRANSITION
            );
      }
    }
    public Interpolator translate(JSONObject jsonObj) throws S2KException {
      String smId = jsonObj.optString("stateMachineId");
      if (smId == null || smId.equals("")) return new ConstantIp("-- Unsupported Region type"); //TODO: support all types
      return new RegionIp(
          smId,
          jsonArrToStringArr(jsonObj.getJSONArray("subvertexIds")),
          jsonArrToStringArr(jsonObj.getJSONArray("transitionIds")));
    }
  }

  protected static class SendSignalActionTr implements Translator {
    protected class SendSignalActionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- SendSignalAction";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement SendSignalActionTr.translate
      return new SendSignalActionIp();
    }
  }

  protected static class SignalTr implements Translator {
    protected class SignalIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Signal";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement SignalTr.translate
      return new SignalIp();
    }
  }

  protected static class SignalEventTr implements Translator {
    protected class SignalEventIp implements ReferenceInterpolator {
      private String referenceName;
      public SignalEventIp(String name) {
        referenceName = name;
      }
      public String interpolate(TranslationMap tm) {
        return String.format(
            "class %1$s extends %2$s",
            referenceName, S2K_EVENT);
      }
      public String reference(TranslationMap tm) {
        return referenceName;
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      return new SignalEventIp(getReference(jsonObj));
    }
  }

  protected static class SlotTr implements Translator {
    protected class SlotIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Slot";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement SlotTr.translate
      return new SlotIp();
    }
  }

  protected static class StateTr implements Translator {
    protected class StateIp implements ReferenceInterpolator {
      private String name;
      public StateIp(String myName) {
        name = myName;
      }
      public String interpolate(TranslationMap tm) {
        return String.format(
            "%1$s : %2$s",
            name, S2K_STATE);
      }
      public String reference(TranslationMap tm) {
        return name;
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      return new StateIp(getReference(jsonObj));
    }
  }

  protected static class StateMachineTr implements Translator {
    protected class StateMachineIp implements ReferenceInterpolator {
      private String name;
      private String[] eventIds;
      public StateMachineIp(String myName, String[] myEventIds) {
        name = myName;
        eventIds = myEventIds;
      }
      public String interpolate(TranslationMap tm) throws S2KException {
        String events = "";
        ReferenceInterpolator eventIp;
        for (String eId : eventIds) {
          eventIp = makeRefIp( tm.get(eId) );
          if (eventIp == null) {
            throw new S2KParseException("Event was referenced but not defined.");
          }
          events += String.format("  %1$s : %2$s%n", eventIp.reference(tm), S2K_EVENT);
        }
        return events;
      }
      public String reference(TranslationMap tm) {
        return name;
      }
    }
    public Interpolator translate(JSONObject jsonObj) throws S2KParseException {
      // TODO: Implement StateMachineTr.translate
      return new StateMachineIp(getReference(jsonObj), jsonArrToStringArr(jsonObj.getJSONArray("eventIds")));
    }
  }

  protected static class TimeEventTr implements Translator {
    protected class TimeEventIp implements ReferenceInterpolator {
      private String referenceName;
      public TimeEventIp(String name) {
        referenceName = name;
      }
      public String interpolate(TranslationMap tm) {
        return String.format(
            "class %1$s extends %2$s",
            referenceName, S2K_EVENT);
      }
      public String reference(TranslationMap tm) {
        return referenceName;
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      return new TimeEventIp(getReference(jsonObj));
    }
  }

  protected static class TimeExpressionTr implements Translator {
    protected class TimeExpressionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- TimeExpression";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement TimeExpressionTr.translate
      return new TimeExpressionIp();
    }
  }

  protected static class TransitionTr implements Translator {
    protected class TransitionIp implements Interpolator {
      private String sourceId, targetId;
      public TransitionIp(String mySourceId, String myTargetId) {
        sourceId = mySourceId;
        targetId = myTargetId;
      }
      public String interpolate(TranslationMap tm) throws S2KException {
        return String.format(
            "req%n"
          + "  forall S2K_event : %3$s :-%n"
          + "    %4$s(%1$s, S2K_event) = %2$s",
          makeRefIp(tm.get(sourceId)).reference(tm),
          makeRefIp(tm.get(targetId)).reference(tm),
          S2K_EVENT,
          S2K_TRANSITION
          );
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      return new TransitionIp(
          jsonObj.getString("sourceId"),
          jsonObj.getString("targetId"));
    }
  }

  protected static class TriggerTr implements Translator {
    protected class TriggerIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Trigger";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement TriggerTr.translate
      return new TriggerIp();
    }
  }
}