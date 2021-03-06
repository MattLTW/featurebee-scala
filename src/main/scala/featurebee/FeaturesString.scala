package featurebee

import featurebee.api.Feature
import featurebee.api.Feature.FeatureName

import scala.util.Try

object FeaturesString {

  private def lowerCaseKeys[T](m: Map[String,T]) = m.map { case (k, v) => (k.toLowerCase, v) }

  /**
   * Parses the string for a forced activation of features.
   *
   * @param featuresString usually has a format like: feature1=true|feature2=false|feature3=true, where the feature name
    *                       should be case insensitive
   *
   * @return a function that can check if a given feature should be forced to be in the given state (true| false). The
    *         feature name is case insensitive.
   */
  def parseForcedFeaturesString(featuresString: String): FeatureName => Option[Boolean] = {
    featureName =>
      val map = featuresString.split('|').toList.flatMap {
        singleFeature =>
          singleFeature.split('=').toList match {
            case key :: value :: Nil if Try(value.toBoolean).isSuccess => Some(key.toLowerCase -> value.toBoolean)
            case other => None // wrong feature format. e.g. name=uh
          }
      }.toMap

      lowerCaseKeys(map).get(featureName.toLowerCase)
  }

  /**
    * Builds a feature string from a set of features and the current clientInfo.
    * @param features The Features to build the string from.
    * @param clientInfo The current client info
    * @return A feature string in the format of feature1=true|feature2=false|feature3=true. Where all feature objects in
    *         `features` are covered in the output.
    * @throws IllegalArgumentException When features contains a feature that does not have a description.
    */
  def buildFeaturesString(features: Traversable[Feature])(implicit clientInfo: ClientInfo): String = {
    if (features.exists { feature => feature.featureDescription.isEmpty }) {
      throw new IllegalArgumentException("Cannot build feature String from features not containing a feature description")
    }

    features.map { (feature) =>
      s"${feature.featureDescription.get.name}=${feature.isActive}"
    }.mkString("|")
  }
}
