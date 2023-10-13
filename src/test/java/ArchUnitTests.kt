import com.conference.location.LocationInfoStreamer
import com.conference.presentations.Presentation
import com.conference.rooms.Room
import com.tngtech.archunit.base.DescribedPredicate.equalTo
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT
import com.tngtech.archunit.core.domain.properties.HasModifiers
import com.tngtech.archunit.core.domain.properties.HasType
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests
import com.tngtech.archunit.core.importer.Location
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import com.tngtech.archunit.library.freeze.FreezingArchRule
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AnalyzeClasses(
    packages = ["com.conference"],
    importOptions = [DoNotIncludeLogging::class, DoNotIncludeTests::class]
)
internal class ArchUnitTests {

    @ArchTest
    fun `system err and system out should be used`(classes: JavaClasses) {
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(classes)
    }

    @ArchTest
    fun `generic exceptions should not be thrown`(classes: JavaClasses) {
        NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(classes)
    }

    @ArchTest
    fun `interfaces should not have the word interface in their name`(classes: JavaClasses) {
        noClasses()
            .that()
            .areInterfaces()
            .should()
            .haveSimpleNameContaining("Interface")
            .check(classes)
    }

    @ArchTest
    fun `speaker class should always be abstract`(classes: JavaClasses) {
        val speakerClassRule = classes()
            .that()
            .resideInAPackage("..speakers")
            .and()
            .haveSimpleName("Speaker")
            .should()
            .haveModifier(ABSTRACT)

        FreezingArchRule.freeze(speakerClassRule).check(classes)
    }

    @ArchTest
    fun `presentation interface should only have two implementations`(classes: JavaClasses) {
        classes()
            .that()
            .implement(Presentation::class.java)
            .should()
            .containNumberOfElements(equalTo(2))
            .because("only two presentations are allowed according to our design. We might keep just one")
            .check(classes)
    }

    @ArchTest
    fun `LocalPresentation should not depend on InternationalPresentation`(classes: JavaClasses) {
        noClasses()
            .that()
            .haveSimpleName("LocalPresentation")
            .should()
            .dependOnClassesThat()
            .haveSimpleName("InternationalPresentation")
            .check(classes)
    }

    @ArchTest
    fun `classes in location package should not depend on classes in speakers package`(classes: JavaClasses) {
        noClasses()
            .that()
            .resideInAPackage("..location")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..speakers")
            .because("speakers may have talks in different locations")
            .check(classes)
    }

    @ArchTest
    fun `all classes under tickets should not depend on each other`(classes: JavaClasses) {
        slices()
            .matching("..tickets.(*)..")
            .should()
            .notDependOnEachOther()
            .check(classes)
    }

    @ArchTest
    fun `Room implementations should have a serialVersionUID field`(classes: JavaClasses) {
        classes()
            .that()
            .implement(Room::class.java)
            .should(HaveAValidSerialVersionUidField())
            .because("Room interface is serializable")
            .check(classes)
    }

    @ArchTest
    fun `all package names should be in lower case`(classes: JavaClasses) {
        classes.forEach {
            assert(it.packageName === it.packageName.lowercase())
        }
    }

    @ArchTest
    fun `methods annotated with LocationInfoStreamer should have between 1 and 3 arguments`(classes: JavaClasses) {
        methods().that()
            .areAnnotatedWith(LocationInfoStreamer::class.java)
            .should(HaveValidArguments())
            .check(classes)
    }

}

private class DoNotIncludeLogging : ImportOption {
    override fun includes(location: Location?): Boolean {
        return when (location) {
            null -> false
            else -> !location.contains("logging")
        }
    }
}

private class HaveAValidSerialVersionUidField() : ArchCondition<JavaClass>("have a valid serialVersionUID field") {
    override fun check(item: JavaClass?, events: ConditionEvents?) {
        val serialVersionField = item?.getField("serialVersionUID")

        val isSerialVersionValid = (HasModifiers.Predicates.modifier(JavaModifier.STATIC).test(serialVersionField)
                && HasModifiers.Predicates.modifier(JavaModifier.FINAL).test(serialVersionField)
                && HasType.Predicates.rawType(UUID::class.java).test(serialVersionField))

        events?.add(SimpleConditionEvent(item, isSerialVersionValid, "serialVersionUID is not valid"))
    }
}

private class HaveValidArguments() :
    ArchCondition<JavaMethod>("methods using LocationInfoStreamer annotation should have valid parameters") {

    override fun check(method: JavaMethod, events: ConditionEvents) {
        val numberOfArgumentsRule = object {
            val message = "method ${method.name} should specify between 1 and 3 arguments when annotation is used"
            val isValid = method.parameters.size in 1..3
        }

        if (!numberOfArgumentsRule.isValid) {
            events.add(SimpleConditionEvent.violated(method, numberOfArgumentsRule.message))
        }
    }
}
