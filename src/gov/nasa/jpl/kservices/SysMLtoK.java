package gov.nasa.jpl.kservices;

//import org.json.simple.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nasa.jpl.mbee.util.FileUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.FileReader;

//import sysml.json_impl.JsonSystemModel;
//import sysml.json_impl.JsonElement;
//import sysml.json_impl.JsonBlock;
//import sysml.json_impl.JsonConstraintBlock;
//import sysml.json_impl.JsonPart;
//import sysml.json_impl.JsonConstraintParameter;
//import sysml.json_impl.JsonConstraintProperty;
//import sysml.json_impl.JsonParametricDiagram;
//import sysml.json_impl.JsonBindingConnector;
//import sysml.json_impl.JsonBaseElement;
//import sysml.json_impl.JsonProject;
//import sysml.json_impl.JsonStereotype;
//import sysml.json_impl.JsonProperty;
//import sysml.json_impl.JsonPropertyValues;
//import sysml.json_impl.JsonValueProperty;
//import sysml.json_impl.JsonValueType;
//import sysml.json_impl.JsonGraphElement;


public class SysMLtoK {
  /**
   * Mapping from sysmlid -> element translation in K
   */
  public static class TranslationMap extends HashMap<String,Interpolator>{};
  
  /**
   * For translation of sysml elements to K fragments.
   */
  public interface Translator {
    /**
     * Perform initial element translation, generating a fragment.
     * @param jsonObj The element to translate.
     * @throws JSONException if element 
     */
    public Interpolator translate(JSONObject jsonObj) throws JSONException, S2KParseException;
  }
  
  /**
   * For interpolation of translated fragments.
   */
  public interface Interpolator {
    /**
     * Perform the interpolation and return the result.
     * @param tm TranslationMap for all fragments
     * @return Resultant code
     */
    public String interpolate(TranslationMap tm);
  }
  
  /**
   * Simple interpolator for elements with no dependencies.
   */
  public static class ConstantIp implements Interpolator {
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
  public static class FormatIp extends ConstantIp {
    public FormatIp(String format, Object... args) {
      super(String.format(format, args));
    }
  }
  
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
   */
  public static TranslationMap translateElements(JSONObject jsonObj) throws S2KParseException {
    try {
      TranslationMap translationMap = new TranslationMap();
      JSONArray      jsonElements   = jsonObj.getJSONArray("elements");
      JSONObject element;
      String     elId;
      for (int i = 0; i < jsonElements.length(); ++i) {
        element = jsonElements.getJSONObject(i);
        try {
          elId = element.getString("sysmlid");
        } catch (JSONException e) {
          // If the new format failed, try the old format.
          // If this fails, fall through to "unsupported structure"
          elId = element.getString("id");
        }
        translationMap.put(elId, translateElement(element));
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
   */
  public static Interpolator translateElement(JSONObject element) throws S2KParseException {
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
  
  // Define each translation individually:
  // For brevity and clarity, all translators should be named "sysml element type" + "Tr"
  // For consistency, all translators should be added to this file in alphabetical order
  // TODO: decide on placeholder format, and implement in every method

  public static class AcceptEventActionTr implements Translator {
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

  public static class ActivityTr implements Translator {
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

  public static class ActivityFinalNodeTr implements Translator {
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

  public static class ActivityParameterNodeTr implements Translator {
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

  public static class AssociationTr implements Translator {
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

  public static class AssociationClassTr implements Translator {
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

  public static class CallBehaviorActionTr implements Translator {
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

  public static class ChangeEventTr implements Translator {
    protected class ChangeEventIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- ChangeEvent";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ChangeEventTr.translate
      return new ChangeEventIp();
    }
  }

  public static class ClassTr implements Translator {
    protected class ClassIp implements Interpolator {
      private String name;
      private Interpolator contentIp;
      public ClassIp(String myName, JSONObject contentJson) throws S2KParseException {
        name = myName;
        contentIp = translateElement(contentJson);
      }
      public String interpolate(TranslationMap tm) {
        // TODO: figure out what needs to be called for interpolation
        return String.format(
            "class %1$s {%n"
          + "  %2$s%n"
          + "}",
            name,
            contentIp.interpolate(tm));
      }
    }
    public Interpolator translate(JSONObject jsonObj) throws S2KParseException, JSONException {
      return new ClassIp(jsonObj.getString("name"), jsonObj.getJSONObject("_contents"));
    }
  }

  public static class CommentTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      return new FormatIp("-- %s",jsonObj.getString("body"));
    }
  }

  public static class ConnectorTr implements Translator {
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

  public static class ConnectorEndTr implements Translator {
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

  public static class ConstraintTr implements Translator {
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

  public static class ControlFlowTr implements Translator {
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

  public static class DecisionNodeTr implements Translator {
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

  public static class DependencyTr implements Translator {
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

  public static class DiagramTr implements Translator {
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

  public static class DurationTr implements Translator {
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

  public static class DurationConstraintTr implements Translator {
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

  public static class DurationIntervalTr implements Translator {
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

  public static class DurationObservationTr implements Translator {
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

  public static class ElementValueTr implements Translator {
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

  public static class ExpressionTr implements Translator {
    protected class ExpressionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Expression";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement ExpressionTr.translate
      return new ExpressionIp();
    }
  }

  public static class FinalStateTr implements Translator {
    protected class FinalStateIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- FinalState";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement FinalStateTr.translate
      return new FinalStateIp();
    }
  }

  public static class ForkNodeTr implements Translator {
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

  public static class GeneralizationTr implements Translator {
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

  public static class InitialNodeTr implements Translator {
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

  public static class InputPinTr implements Translator {
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

  public static class InstanceSpecificationTr implements Translator {
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

  public static class InstanceValueTr implements Translator {
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

  public static class InterruptibleActivityRegionTr implements Translator {
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

  public static class JoinNodeTr implements Translator {
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

  public static class LiteralBooleanTr implements Translator {
    protected class LiteralBooleanIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- LiteralBoolean";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement LiteralBooleanTr.translate
      return new LiteralBooleanIp();
    }
  }

  public static class LiteralIntegerTr implements Translator {
    protected class LiteralIntegerIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- LiteralInteger";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement LiteralIntegerTr.translate
      return new LiteralIntegerIp();
    }
  }

  public static class LiteralRealTr implements Translator {
    protected class LiteralRealIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- LiteralReal";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement LiteralRealTr.translate
      return new LiteralRealIp();
    }
  }

  public static class LiteralStringTr implements Translator {
    protected class LiteralStringIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- LiteralString";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement LiteralStringTr.translate
      return new LiteralStringIp();
    }
  }

  public static class LiteralUnlimitedNaturalTr implements Translator {
    protected class LiteralUnlimitedNaturalIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- LiteralUnlimitedNatural";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement LiteralUnlimitedNaturalTr.translate
      return new LiteralUnlimitedNaturalIp();
    }
  }

  public static class MergeNodeTr implements Translator {
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

  public static class ModelTr implements Translator {
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

  public static class MountTr implements Translator {
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

  public static class ObjectFlowTr implements Translator {
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

  public static class OpaqueActionTr implements Translator {
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

  public static class OpaqueExpressionTr implements Translator {
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

  public static class OperationTr implements Translator {
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

  public static class OutputPinTr implements Translator {
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

  public static class PackageTr implements Translator {
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

  public static class ParameterTr implements Translator {
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

  public static class PortTr implements Translator {
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

  public static class ProfileApplicationTr implements Translator {
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

  public static class ProjectTr implements Translator {
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

  public static class PropertyTr implements Translator {
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: translate the typeId into a meaningful type name
      return new FormatIp(
          "%1$s : %2$s",
          jsonObj.getString("name"),
          jsonObj.getString("typeId"));
    }
  }

  public static class PseudostateTr implements Translator {
    protected class PseudostateIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Pseudostate";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement PseudostateTr.translate
      return new PseudostateIp();
    }
  }

  public static class RegionTr implements Translator {
    protected class RegionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Region";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement RegionTr.translate
      return new RegionIp();
    }
  }

  public static class SendSignalActionTr implements Translator {
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

  public static class SignalTr implements Translator {
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

  public static class SignalEventTr implements Translator {
    protected class SignalEventIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- SignalEvent";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement SignalEventTr.translate
      return new SignalEventIp();
    }
  }

  public static class SlotTr implements Translator {
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

  public static class StateTr implements Translator {
    protected class StateIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- State";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement StateTr.translate
      return new StateIp();
    }
  }

  public static class StateMachineTr implements Translator {
    protected class StateMachineIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- StateMachine";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement StateMachineTr.translate
      return new StateMachineIp();
    }
  }

  public static class TimeEventTr implements Translator {
    protected class TimeEventIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- TimeEvent";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement TimeEventTr.translate
      return new TimeEventIp();
    }
  }

  public static class TimeExpressionTr implements Translator {
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

  public static class TransitionTr implements Translator {
    protected class TransitionIp implements Interpolator {
      public String interpolate(TranslationMap tm) {
        return "-- Transition";
      }
    }
    public Interpolator translate(JSONObject jsonObj) {
      // TODO: Implement TransitionTr.translate
      return new TransitionIp();
    }
  }

  public static class TriggerTr implements Translator {
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