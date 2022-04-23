package co.skyclient.scc.gui.greeting

import cc.woverflow.onecore.utils.browseURL
import club.sk1er.patcher.config.PatcherConfig
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.ChatColor
import gg.essential.vigilance.gui.settings.ButtonComponent
import co.skyclient.scc.gui.greeting.components.CorrectOutsidePixelConstraint
import co.skyclient.scc.gui.greeting.components.GreetingSlide
import co.skyclient.scc.SkyclientCosmetics
import co.skyclient.scc.utils.TickDelay
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.universal.UDesktop
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.MathHelper
import java.awt.Color
import java.lang.reflect.Field
import java.lang.reflect.Method

class OptimizationSlide : GreetingSlide<HUDChachySlide>(HUDChachySlide::class.java) {
    init {
        hideNextButton()
    }

    val text by UIWrappedText("""
        Would you like to apply pre-optimized settings?
    """.trimIndent(), centered = true) constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 75.percent()
        textScale = 3.pixels()
    } childOf window

    val secondaryText by UIWrappedText("""
        Click here for a full explanation on what this changes.
    """.trimIndent(), centered = true) constrain {
        x = CenterConstraint()
        y = SiblingConstraint(5f).also { it.constrainTo = text }
        width = 100.percent()
    } childOf window

    init {
        secondaryText.onLeftClick { UDesktop.browseURL("https://github.com/nacrt/SkyblockClient-REPO/blob/main/files/guides/skyclient_greeting_optimizer.md") }
    }

    val progressText by UIText() constrain {
        color = Color.GREEN.darker().toConstraint()
        x = CenterConstraint()
        y = 2.pixels(alignOpposite = true)
    } childOf window

    val yesButton by ButtonComponent("${ChatColor.GREEN}Yes") {
        progressText.setText("Applying optimized settings... This might take a while...")
        TickDelay(2) {
            if (isOptifineLoaded()) {
                try {
                    val settingsClass: Class<GameSettings> = mc.gameSettings.javaClass
                    val configClass = Class.forName("Config")
                    val shadersClass = Class.forName("net.optifine.shaders.Shaders")
                    settingsClass.getFieldAndSetAccessible("ofFastRender")?.setBooleanSafe(mc.gameSettings, false)
                    configClass.getMethodAndSetAccessible("updateFramebufferSize")?.invokeSafe(null)

                    settingsClass.getFieldAndSetAccessible("ofFastMath")?.setBooleanSafe(mc.gameSettings, false)
                    MathHelper::class.java.getFieldAndSetAccessible("fastMath")?.setBooleanSafe(null, false)

                    settingsClass.getFieldAndSetAccessible("ofSmartAnimations")?.setBooleanSafe(mc.gameSettings, true)
                    settingsClass.getFieldAndSetAccessible("ofRenderRegions")?.setBooleanSafe(mc.gameSettings, true)
                    settingsClass.getFieldAndSetAccessible("ofSmoothFps")?.setBooleanSafe(mc.gameSettings, false)
                    settingsClass.getFieldAndSetAccessible("ofSmoothWorld")?.setBooleanSafe(mc.gameSettings, false)
                    configClass.getMethodAndSetAccessible("updateThreadPriorities")?.invokeSafe(null)

                    settingsClass.getFieldAndSetAccessible("ofClouds")?.setIntSafe(mc.gameSettings, 0)
                    settingsClass.getMethodAndSetAccessible("updateRenderClouds")?.invokeSafe(mc.gameSettings)
                    mc.renderGlobal.javaClass.getMethodAndSetAccessible("resetClouds")?.invokeSafe(mc.renderGlobal)

                    settingsClass.getFieldAndSetAccessible("ofFogType")?.setIntSafe(mc.gameSettings, 0)
                    val connectedTextures = settingsClass.getFieldAndSetAccessible("ofConnectedTextures")
                    try {
                        if (connectedTextures?.getInt(null) == 2) {
                            connectedTextures.setIntSafe(mc.gameSettings, 1)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    settingsClass.getFieldAndSetAccessible("ofTranslucentBlocks")?.setIntSafe(mc.gameSettings, 1)
                    settingsClass.getFieldAndSetAccessible("ofDroppedItems")?.setIntSafe(mc.gameSettings, 1)
                    settingsClass.getFieldAndSetAccessible("ofVignette")?.setIntSafe(mc.gameSettings, 1)
                    settingsClass.getFieldAndSetAccessible("ofSwampColors")?.setBooleanSafe(mc.gameSettings, false)
                    Class.forName("net.optifine.CustomColors").getMethodAndSetAccessible("updateUseDefaultGrassFoliageColors")
                        ?.invokeSafe(null)

                    settingsClass.getFieldAndSetAccessible("ofRain")?.setIntSafe(mc.gameSettings, 1)
                    shadersClass.getMethodAndSetAccessible("setShaderPack")?.invokeSafe(null, "OFF")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (SkyclientCosmetics.isPatcher) {
                PatcherConfig.cullingFix = true
                PatcherConfig.separateResourceLoading = true
                PatcherConfig.disableAchievements = true
                PatcherConfig.autoTitleScale = true
                PatcherConfig.unfocusedFPS = true
                PatcherConfig.cleanProjectiles = true
                PatcherConfig.numericalEnchants = true
                PatcherConfig.staticItems = true
                PatcherConfig.limitChunks = true
                PatcherConfig.playerBackFaceCulling = true
                PatcherConfig.openToLanReplacement = 1
                PatcherConfig.INSTANCE.markDirty()
                PatcherConfig.INSTANCE.writeData()
            }
            mc.gameSettings.saveOptions()
            if (mc.gameSettings.entityShadows) {
                mc.gameSettings.setOptionValue(GameSettings.Options.ENTITY_SHADOWS, 0)
            }
            if (!mc.gameSettings.useVbo) {
                mc.gameSettings.setOptionValue(GameSettings.Options.USE_VBO, 1)
            }
            if (mc.gameSettings.enableVsync) {
                mc.gameSettings.setOptionValue(GameSettings.Options.ENABLE_VSYNC, 0)
            }
            mc.gameSettings.setOptionFloatValue(GameSettings.Options.FRAMERATE_LIMIT, 260f)
            mc.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, 10)
            mc.gameSettings.guiScale = 3
            mc.gameSettings.saveOptions()
            mc.gameSettings.loadOptions()
            mc.refreshResources()
            mc.renderGlobal.loadRenderers()
            TickDelay(2) {
                Window.enqueueRenderOperation { displayNextScreen() }
            }
        }
    } constrain {
        y = CenterConstraint()
        x = CorrectOutsidePixelConstraint(window.getWidth() / 2 - 2)
    } childOf blackbar

    val noButton by ButtonComponent("${ChatColor.RED}No") {
        displayNextScreen()
    } constrain {
        y = CenterConstraint()
        x = (window.getWidth() / 2 + 2).pixels()
    } childOf blackbar

    override fun setButtonFloat() {
        yesButton.setFloating(true)
        noButton.setFloating(true)
    }

    private fun Class<*>.getFieldAndSetAccessible(name: String): Field? {
        return try {
            val field = this.getDeclaredField(name)
            field.isAccessible = true
            field
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun Class<*>.getMethodAndSetAccessible(name: String): Method? {
        return try {
            val method = this.getDeclaredMethod(name)
            method.isAccessible = true
            method
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun Field.setBooleanSafe(obj: Any?, value: Boolean) {
        try {
            this.setBoolean(obj, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Field.setIntSafe(obj: Any?, value: Int) {
        try {
            this.setInt(obj, value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Method.invokeSafe(obj: Any?, vararg strings: String) {
        try {
            this.invoke(obj, strings)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isOptifineLoaded(): Boolean {
        return try {
            val clazz = Class.forName("Config")
            clazz.getDeclaredField("OF_RELEASE")
            true
        } catch (_: Exception) {
            false
        }
    }
}