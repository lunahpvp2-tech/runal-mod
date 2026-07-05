plugins {
    id("dev.kikugie.loom-back-compat")
    id("maven-publish")
}

version = "${property("mod.version")}+${sc.current.version}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val requiredJava: JavaVersion = JavaVersion.toVersion(sc.properties.get<String>("mod.java_major"))

// Source is written against the 26.1.2 API; these renames map 1:1 onto the real Yarn-era names
// (1.21.4 and 1.21.11 share the same API surface here) with identical behaviour, so a straight
// identifier swap is enough (no //? if needed).
val is1214 = sc.current.version == "1.21.4" || sc.current.version == "1.21.11"
// A handful of renames (ResourceLocation, PlayerRenderer, RenderType, getPosition/getXRot/getYRot,
// getName/getId) only actually happened in 1.21.4 - Mojang reverted or never made these changes by
// 1.21.11, so on 1.21.11 those specific APIs still match the fictional 26.1.2 style.
val only1214 = sc.current.version == "1.21.4"
sc.replacements {
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
        // Negative lookahead avoids matching "AvatarRendererMixin" (the mixin class's own name,
        // which must stay stable across versions since runal.client.mixins.json references
        // it literally and isn't itself run through this text-swap pipeline).
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
    // Add repositories to retrieve artifacts from in here.
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
    // Applies Mojang's official mappings; loom-back-compat picks the matching Loom variant per version.
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader") as String}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")

    // MixinExtras - needed for @ModifyExpressionValue and similar advanced mixin injectors
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
