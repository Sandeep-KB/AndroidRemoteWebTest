package sandeep.kb.android.remote.android;

import sandeep.kb.android.remote.constants.AndroidWebDriverConstants;
import sandeep.kb.android.remote.utils.Utils;

/**
 * The WebDriver atoms are used to ensure consistent behaviour cross-browser.
 * 
 * The atoms are saved in res folder, the path for each of them is obtained by this enum.
 * At runtime they are copied as required.
 * 
 * @author Sandeep
 */
public enum AndroidAtoms {

  // AUTO GENERATED - DO NOT EDIT BY HAND

  EXECUTE_ASYNC_SCRIPT( "EXECUTE_ASYNC_SCRIPT"), 
  GET_APPCACHE_STATUS("GET_APPCACHE_STATUS"), 
  EXECUTE_SCRIPT("EXECUTE_SCRIPT"), 
  CLICK("CLICK"),
  SUBMIT("SUBMIT"),
  CLEAR("CLEAR"),
  GET_ATTRIBUTE_VALUE("GET_ATTRIBUTE_VALUE"),
  IS_SELECTED("IS_SELECTED"),
  IS_ENABLED("IS_ENABLED"),
  GET_TEXT("GET_TEXT"),
  FIND_ELEMENTS("FIND_ELEMENTS"),
  FIND_ELEMENT("FIND_ELEMENT"),
  GET_TOP_LEFT_COORDINATES("GET_TOP_LEFT_COORDINATES"),
  GET_SIZE("GET_SIZE"),
  IS_DISPLAYED("IS_DISPLAYED"), 
  GET_VALUE_OF_CSS_PROPERTY( "GET_VALUE_OF_CSS_PROPERTY"),
;

  private final String value;

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return getValue();
  }

  AndroidAtoms(String value) {
    this.value = Utils.readDataFromFile(AndroidWebDriverConstants.ATOM_PATH+value+AndroidWebDriverConstants.FILE_EXTN_ATOM);
  }


}