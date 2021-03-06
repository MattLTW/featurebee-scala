package featurebee

import featurebee.api._
import featurebee.impl._
import org.scalatest.FeatureSpec
import org.scalatest.OptionValues._
import org.mockito.Mockito._

class FeatureImplSpec extends FeatureSpec {

  val emptyClientInfo = ClientInfoImpl()

  feature("Standard feature activation") {
    scenario("Feature is active when always on condition is used") {
      val featureDescription = FeatureDescription("name", "desc", tags = None, Set(AlwaysOnCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === true)
    }

    scenario("Feature is not active when always off condition is used") {
      val featureDescription = FeatureDescription("name", "desc", tags = None, Set(AlwaysOffCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === false)
    }

    scenario("FeatureImpl throws IllegalArgumentException when no condition is used") {
      intercept[IllegalArgumentException] {
        FeatureDescription("name", "desc", tags = None, Set())
      }
    }

    scenario("Feature is not active when not all conditions are met") {
      val featureDescription = FeatureDescription("name", "desc", tags = None, Set(AlwaysOffCondition, AlwaysOnCondition))
      assert(new FeatureImpl(featureDescription).isActive(emptyClientInfo) === false)
    }
  }

  feature("God mode overriding of feature settings") {
    scenario("Overriding has precedence") {
      val clientInfoForcedAlwaysOn = ClientInfoImpl(forcedFeatureToggle = (_) => Some(true))
      val featureDescription = FeatureDescription("name", "desc", tags = None, Set(AlwaysOffCondition))
      assert(new FeatureImpl(featureDescription).isActive(clientInfoForcedAlwaysOn) === true)
    }
  }

  feature("Using block convenience methods in Feature for chrome only feature")  {
    val featureDescriptionChromeOnly = FeatureDescription("name", "desc", tags = None,
      Set(UserAgentCondition(Set("Chrome"))))
    val feature = new FeatureImpl(featureDescriptionChromeOnly)

    scenario("Feature block 'ifActive' for chrome clients is executed") {
      implicit val clientInfo = ClientInfoImpl(Some("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"))
      var evaluated = false

      val result = feature.ifActive {
        evaluated = true
        "feature block has been run"
      }

      assert(result.value === "feature block has been run")
      assert(evaluated === true)
    }

    scenario("Feature block 'notActive' for non chrome clients is executed") {
      implicit val clientInfo = ClientInfoImpl(Some("Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0"))
      var evaluated = false

      val result = feature.ifNotActive {
        evaluated = true
        "feature block has been run"
      }

      assert(result.value === "feature block has been run")
      assert(evaluated === true)
    }
  }

  feature("Fetching features from Registry") {
    scenario("..via apply() works") {
      val featureName: String = "feature-1"

      val featureReg = mock(classOf[FeatureRegistry])
      val feature = mock(classOf[Feature])
      when(featureReg.feature(featureName)).thenReturn(Some(feature))

      assert(Feature(featureName)(featureReg) === Some(feature))
    }
  }

  feature("AlwaysOn/Off Feature") {
    scenario("AlwaysOnFeature always activates feature") {
      val clientInfo = ClientInfoImpl(forcedFeatureToggle = (_) => Some(false))
      assert(AlwaysOnFeature.isActive(clientInfo) === true)
    }

    scenario("AlwaysOffFeature always deactivates feature") {
      val clientInfo = ClientInfoImpl(forcedFeatureToggle = (_) => Some(true))
      assert(AlwaysOffFeature.isActive(clientInfo) === false)
    }
  }
}