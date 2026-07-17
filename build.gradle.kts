plugins {
    id("dev.kikugie.loom-back-compat")
    id("maven-publish")
}

version = "${property("mod.version")}+${sc.current.version}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = JavaVersion.toVersion(sc.properties.get<String>("mod.java_major"))

val is1214 = sc.current.version == "1.21.4" || sc.current.version == "1.21.11"
val only1214 = sc.current.version == "1.21.4"
val only262 = sc.current.version == "26.2"
sc.replacements {
    regex {
        direction.set(only262)
        replace("""\.setScreen\(""", ".setScreenAndShow(", """\.setScreenAndShow\(""", ".setScreen(")
    }
    regex {
        direction.set(only262)
        replace("""\.getMainCamera\(\)""", ".mainCamera()", """\.mainCamera\(\)""", ".getMainCamera()")
    }
    string {
        direction.set(only1214)
        replace("Identifier", "ResourceLocation")
    }
    string {
        direction.set(is1214)
        replace("GuiGraphicsExtractor", "GuiGraphics")
    }
    string {
        direction.set(only1214)
        replace("AvatarRenderState", "PlayerRenderState")
    }
    regex {
        direction.set(only1214)
        replace("""AvatarRenderer(?!Mixin)""", "PlayerRenderer", """PlayerRenderer(?!Mixin)""", "AvatarRenderer")
    }
    string {
        direction.set(only1214)
        replace("RenderTypes", "RenderType")
    }
    string {
        direction.set(is1214)
        replace("KeyMappingHelper", "KeyBindingHelper")
    }
    string {
        direction.set(is1214)
        replace("registerKeyMapping", "registerKeyBinding")
    }
    regex {
        direction.set(is1214)
        replace("""\.text\(""", ".drawString(", """\.drawString\(""", ".text(")
    }
    regex {
        direction.set(is1214)
        replace("""\.centeredText\(""", ".drawCenteredString(", """\.drawCenteredString\(""", ".centeredText(")
    }
    regex {
        direction.set(is1214)
        replace("""\.outline\(""", ".renderOutline(", """\.renderOutline\(""", ".outline(")
    }
    regex {
        direction.set(is1214)
        replace("""\.itemDecorations\(""", ".renderItemDecorations(", """\.renderItemDecorations\(""", ".itemDecorations(")
    }
    regex {
        direction.set(only1214)
        replace("""\.position\(\)""", ".getPosition()", """\.getPosition\(\)""", ".position()")
    }
    regex {
        direction.set(only1214)
        replace("""\.xRot\(\)""", ".getXRot()", """\.getXRot\(\)""", ".xRot()")
    }
    regex {
        direction.set(only1214)
        replace("""\.yRot\(\)""", ".getYRot()", """\.getYRot\(\)""", ".yRot()")
    }
    regex {
        direction.set(only1214)
        replace("""getGameProfile\(\)\.name\(\)""", "getGameProfile().getName()", """getGameProfile\(\)\.getName\(\)""", "getGameProfile().name()")
    }
    regex {
        direction.set(only1214)
        replace("""getProfile\(\)\.name\(\)""", "getProfile().getName()", """getProfile\(\)\.getName\(\)""", "getProfile().name()")
    }
    regex {
        direction.set(only1214)
        replace("""getProfile\(\)\.id\(\)""", "getProfile().getId()", """getProfile\(\)\.getId\(\)""", "getProfile().id()")
    }
    regex {
        direction.set(is1214)
        replace("""\.item\(""", ".renderItem(", """\.renderItem\(""", ".item(")
    }
}

repositories {
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("runal") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader") as String}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")

    implementation("io.github.llamalad7:mixinextras-fabric:0.5.2")
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.5.2")
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava.majorVersion.toInt()
}

tasks.withType<ProcessResources>().configureEach {
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft" to sc.properties.get<String>("mod.mc_compat"),
        "javaDepends" to sc.properties.get<String>("mod.java_depends")
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }

    val mixinJava = sc.properties.get<String>("mod.java_level")
    inputs.property("mixinJava", mixinJava)
    filesMatching("*.mixins.json") { expand("java" to mixinJava) }
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
    }
}
