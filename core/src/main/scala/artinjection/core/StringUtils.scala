package artinjection.core

/**
 * @author Julia Astakhova
 */
object StringUtils {

  def clean(str: String, pattern: String) = pattern.r.replaceAllIn(str, "")
}
