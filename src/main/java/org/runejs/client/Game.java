package org.runejs.client;

import org.runejs.client.cache.CacheIndex;
import org.runejs.client.cache.CacheArchive;
import org.runejs.client.cache.CacheFileChannel;
import org.runejs.client.frame.*;
import org.runejs.client.frame.console.Console;
import org.runejs.client.input.KeyFocusListener;
import org.runejs.client.input.MouseHandler;
import org.runejs.client.io.Buffer;
import org.runejs.client.language.English;
import org.runejs.client.language.Native;
import org.runejs.client.media.Rasterizer;
import org.runejs.client.media.Rasterizer3D;
import org.runejs.client.media.VertexNormal;
import org.runejs.client.media.renderable.GameObject;
import org.runejs.client.media.renderable.Item;
import org.runejs.client.media.renderable.Model;
import org.runejs.client.media.renderable.Renderable;
import org.runejs.client.media.renderable.actor.*;
import org.runejs.client.message.handler.MessageHandlerRegistry;
import org.runejs.client.message.handler.rs435.RS435HandlerRegistry;
import org.runejs.client.message.outbound.misc.ClickFlashingTabIconOutboundMessage;
import org.runejs.client.message.outbound.widget.container.DragWidgetItemOutboundMessage;
import org.runejs.client.net.*;
import org.runejs.client.net.codec.MessagePacketCodec;
import org.runejs.client.net.codec.runejs435.RuneJS435PacketCodec;
import org.runejs.client.scene.*;
import org.runejs.client.scene.camera.Camera;
import org.runejs.client.scene.camera.CameraRotation;
import org.runejs.client.scene.camera.CutsceneCamera;
import org.runejs.client.scene.camera.SphericalCamera;
import org.runejs.client.scene.util.CollisionMap;
import org.runejs.client.sound.MusicSystem;
import org.runejs.client.sound.SoundSystem;
import org.runejs.client.util.BitUtils;
import org.runejs.client.util.Signlink;
import org.runejs.client.cache.def.*;
import org.runejs.client.cache.media.AnimationSequence;
import org.runejs.client.cache.media.ImageRGB;
import org.runejs.client.cache.media.IndexedImage;
import org.runejs.client.cache.media.TypeFace;
import org.runejs.client.cache.media.gameInterface.GameInterface;
import org.runejs.client.cache.media.gameInterface.GameInterfaceType;
import org.runejs.client.cache.media.gameInterface.InterfaceModelType;
import org.runejs.Configuration;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;

public class Game {

    /**
     * The codec currently in use to encode and decode packets.
     * 
     * TODO (Jameskmonger) add a clear way to use different codecs
     */
    public static final MessagePacketCodec packetCodec = new RuneJS435PacketCodec();
    
    /**
     * The registry that holds all the InboundMessage handlers.
     */
    public static final MessageHandlerRegistry handlerRegistry = new RS435HandlerRegistry();

    /**
     * The main camera, orbiting on a sphere around the player.
     */
    public static final SphericalCamera playerCamera = new SphericalCamera();

    /**
     * A customisable cutscene camera.
     */
    public static final CutsceneCamera cutsceneCamera = new CutsceneCamera();
    public static int anInt784 = 0;
    public static GameInterface chatboxInterface;
    public static GameSocket updateServerSocket;
    public static boolean aBoolean1735 = true;
    public static boolean aBoolean871 = false;
    public static int loginStatus = 0;
    public static int modewhat = 0;
    public static int modewhere = 0;
    public static long aLong1203 = 0L;
    public static int mouseInvInterfaceIndex = 0;
    public static int anInt509 = 0;
    public static boolean aBoolean519 = true;
    public static Class39 mouseCapturer;
    public static int anInt2591 = 0;
    public static int anInt874 = 0;
    public static int destinationY = 0;
    public static Scene currentScene;
    public static int gameStatusCode = 0;
    private static int gameServerPort;
    private static int duplicateClickCount = 0;
    private static int lastClickY = 0;
    private static int lastClickX = 0;

    /**
     * Minimap rotation is always based on game camera
     */
    public static int getMinimapRotation() {
        return playerCamera.getRotation().yaw;
    }

    public static int anInt1756 = 0;
    public static int menuOffsetY;
    public static int anInt1769 = -1;
    public static int widgetSelected = 0;
    public static Signlink signlink;
    public static CacheIndex metaIndex;
    public static CacheFileChannel dataChannel;
    public static CacheFileChannel metaChannel;
    public static CacheFileChannel[] indexChannels = new CacheFileChannel[13];
    public static int currentPort;
    private static int drawCount = 0;

    private GameErrorHandler errorHandler;

    /**
     * This method is used to draw interfaces on the client. It uses the parent of -1,
     * which means it will render the widget on the top most level. It takes in a widget ID
     * and finds its children within the cached interfaces.
     *
     * @param areaId Mostly used for logical checks
     *   0 = Game area (the area that renders in 3D),
     *   1 = Tab area (the widgets that display within the tab area),
     *   2 = Chat area (the chat itself, as well as all sorts of dialogues and anything that renders over the chat)
     *   3 = Permanent chat widget area (walkable chat widgets that replace the actual chat itself)
     *   4 = ??? walkable widget?
     * @param minX The top right X of this widget's boundaries
     * @param minY The top right Y of this widget's boundaries
     * @param maxX The bottom right Y of this widget's boundaries
     * @param maxY The bottom right X of this widget's boundaries
     * @param widgetId The widget ID
     * @return The status of the drawing cycle, true for success and false for failure
     */
    public static boolean drawParentInterface(int areaId, int minX, int minY, int maxX, int maxY, int widgetId) {
        if(!GameInterface.decodeGameInterface(widgetId))
            return false;

        return drawInterface(areaId, minX, minY, maxX, maxY, 0, 0, GameInterface.cachedInterfaces[widgetId], -1, true);
    }

    /**
     * Recursive function that draws all the widgets within a GameInterface array.
     *
     * TODO (James) make this use GameInterfaceArea for the areaId param
     *
     * @param areaId Mostly used for logical checks
     *   0 = Game area (the area that renders in 3D),
     *   1 = Tab area (the widgets that display within the tab area),
     *   2 = Chat area (the chat itself, as well as all sorts of dialogues and anything that renders over the chat)
     *   3 = Permanent chat widget area (walkable chat widgets that replace the actual chat itself)
     * @param minX The top right X of this widget's boundaries
     * @param minY The top right Y of this widget's boundaries
     * @param maxX The bottom right X of this widget's boundaries
     * @param maxY The bottom right Y of this widget's boundaries
     * @param scrollPosition The current scroll position within this widget
     * @param scrollWidth The current scroll width within this widget (unused)
     * @param interfaceCollection The GameInterface collection to draw
     * @param parentId The parent ID, this will offset the widget's coordinates within its parent
     * @param drawSuccess The status of the drawing cycle, true for success and false for failure
     * @return The status of the drawing cycle, true for success and false for failure
     */
    public static boolean drawInterface(int areaId, int minX, int minY, int maxX, int maxY, int scrollPosition, int scrollWidth, GameInterface[] interfaceCollection, int parentId, boolean drawSuccess) {
        Rasterizer.setBounds(minX, minY, maxX, maxY);

        boolean result = drawSuccess;
        for (int i = 0; interfaceCollection.length > i; i++) {
            GameInterface gameInterface = interfaceCollection[i];
            if (gameInterface != null && gameInterface.parentId == parentId) {
                if (gameInterface.contentType > 0)
                    GameInterface.updateGameInterface(gameInterface);
                int absoluteX = minX + gameInterface.currentX;
                if (!gameInterface.lockScroll)
                    absoluteX -= scrollWidth;
                int absoluteY = minY + gameInterface.currentY;
                if (!gameInterface.lockScroll)
                    absoluteY -= scrollPosition;
                int opacity = gameInterface.opacity;
                if (gameInterface == GameInterface.aGameInterface_353) {
                    opacity = 128;
                    GameInterface gameInterface_3_ = GameInterface.method878(gameInterface);
                    int[] is = GameInterface.method247(gameInterface_3_);
                    int[] is_4_ = GameInterface.method247(gameInterface);
                    int i_5_ = MouseHandler.mouseY + -MovedStatics.anInt2621 + is_4_[1] - is[1];
                    if (i_5_ < 0)
                        i_5_ = 0;
                    if (i_5_ + gameInterface.originalHeight > gameInterface_3_.originalHeight)
                        i_5_ = gameInterface_3_.originalHeight + -gameInterface.originalHeight;
                    absoluteY = i_5_ + is[1];
                    int i_6_ = MouseHandler.mouseX + -MovedStatics.anInt1996 + -is[0] + is_4_[0];
                    if (i_6_ < 0)
                        i_6_ = 0;
                    if (i_6_ + gameInterface.originalWidth > gameInterface_3_.originalWidth)
                        i_6_ = -gameInterface.originalWidth + gameInterface_3_.originalWidth;
                    absoluteX = is[0] + i_6_;
                }
                if (!gameInterface.isNewInterfaceFormat || Rasterizer.viewportRight >= absoluteX && Rasterizer.viewportBottom >= absoluteY && Rasterizer.viewportLeft <= absoluteX + gameInterface.originalWidth && absoluteY + gameInterface.originalHeight >= Rasterizer.viewportTop) {
                    if (gameInterface.type == GameInterfaceType.LAYER) {
                        if (gameInterface.isHidden && !GameInterface.isHovering(areaId, i))
                            continue;
                        if (!gameInterface.isNewInterfaceFormat) {
                            if (-gameInterface.originalHeight + gameInterface.scrollHeight < gameInterface.scrollPosition)
                                gameInterface.scrollPosition = -gameInterface.originalHeight + gameInterface.scrollHeight;
                            if (gameInterface.scrollPosition < 0)
                                gameInterface.scrollPosition = 0;
                        }
                        result &= drawInterface(areaId, absoluteX, absoluteY, gameInterface.originalWidth + absoluteX, gameInterface.originalHeight + absoluteY, gameInterface.scrollPosition, gameInterface.scrollWidth, interfaceCollection, i, drawSuccess);
                        if (gameInterface.children != null)
                            result &= drawInterface(areaId, absoluteX, absoluteY, gameInterface.originalWidth + absoluteX, absoluteY + gameInterface.originalHeight, gameInterface.scrollPosition, gameInterface.scrollWidth, gameInterface.children, gameInterface.id, true);
                        Rasterizer.setBounds(minX, minY, maxX, maxY);
                        if (gameInterface.originalHeight < gameInterface.scrollHeight)
                            GameInterface.drawScrollBar(absoluteX + gameInterface.originalWidth, absoluteY, gameInterface.originalHeight, gameInterface.scrollPosition, gameInterface.scrollHeight);
                    }
                    if (gameInterface.type == GameInterfaceType.UNKNOWN) {
                        continue;
                    }
                    if (gameInterface.type == GameInterfaceType.INVENTORY) {
                        int i_7_ = 0;
                        for (int i_8_ = 0; i_8_ < gameInterface.originalHeight; i_8_++) {
                            for (int i_9_ = 0; gameInterface.originalWidth > i_9_; i_9_++) {
                                int i_10_ = (gameInterface.itemSpritePadsX + 32) * i_9_ + absoluteX;
                                int i_11_ = (32 + gameInterface.itemSpritePadsY) * i_8_ + absoluteY;
                                if (i_7_ < 20) {
                                    i_10_ += gameInterface.images[i_7_];
                                    i_11_ += gameInterface.imageX[i_7_];
                                }
                                if (gameInterface.items[i_7_] <= 0) {
                                    if (gameInterface.imageY != null && i_7_ < 20) {
                                        ImageRGB imageRGB = gameInterface.method638(i_7_);
                                        if (imageRGB == null) {
                                            if (GameInterface.aBoolean2177)
                                                result = false;
                                        } else
                                            imageRGB.drawImage(i_10_, i_11_);
                                    }
                                } else {
                                    int i_12_ = 0;
                                    int i_13_ = -1 + gameInterface.items[i_7_];
                                    int i_14_ = 0;
                                    if (-32 + Rasterizer.viewportLeft < i_10_ && Rasterizer.viewportRight > i_10_ && Rasterizer.viewportTop + -32 < i_11_ && Rasterizer.viewportBottom > i_11_ || MovedStatics.activeInterfaceType != 0 && GroundItemTile.selectedInventorySlot == i_7_) {
                                        int i_15_ = 0;
                                        if (MovedStatics.itemSelected == 1 && i_7_ == GameInterface.selectedInventorySlot && gameInterface.id == ISAAC.anInt525)
                                            i_15_ = 16777215;
                                        ImageRGB imageRGB = ItemDefinition.sprite(gameInterface.itemAmounts[i_7_], i_13_, i_15_);
                                        if (imageRGB == null)
                                            result = false;
                                        else {
                                            if (MovedStatics.activeInterfaceType != 0 && GroundItemTile.selectedInventorySlot == i_7_ && gameInterface.id == MovedStatics.modifiedWidgetId) {
                                                i_14_ = MouseHandler.mouseY + -MovedStatics.anInt2798;
                                                i_12_ = MouseHandler.mouseX + -Renderable.anInt2869;
                                                if (i_12_ < 5 && i_12_ > -5)
                                                    i_12_ = 0;
                                                if (i_14_ < 5 && i_14_ > -5)
                                                    i_14_ = 0;
                                                if (Buffer.lastItemDragTime < 5) {
                                                    i_14_ = 0;
                                                    i_12_ = 0;
                                                }
                                                imageRGB.drawImageWithOpacity(i_12_ + i_10_, i_11_ + i_14_, 128);
                                                if (parentId != -1) {
                                                    GameInterface gameInterface_16_ = interfaceCollection[parentId];
                                                    if (Rasterizer.viewportTop > i_14_ + i_11_ && gameInterface_16_.scrollPosition > 0) {
                                                        int i_17_ = MovedStatics.anInt199 * (Rasterizer.viewportTop + -i_11_ - i_14_) / 3;
                                                        if (10 * MovedStatics.anInt199 < i_17_)
                                                            i_17_ = 10 * MovedStatics.anInt199;
                                                        if (gameInterface_16_.scrollPosition < i_17_)
                                                            i_17_ = gameInterface_16_.scrollPosition;
                                                        gameInterface_16_.scrollPosition -= i_17_;
                                                        MovedStatics.anInt2798 += i_17_;
                                                    }
                                                    if (32 + i_11_ + i_14_ > Rasterizer.viewportBottom && -gameInterface_16_.originalHeight + gameInterface_16_.scrollHeight > gameInterface_16_.scrollPosition) {
                                                        int i_18_ = MovedStatics.anInt199 * (-Rasterizer.viewportBottom + 32 + i_11_ + i_14_) / 3;
                                                        if (MovedStatics.anInt199 * 10 < i_18_)
                                                            i_18_ = 10 * MovedStatics.anInt199;
                                                        if (-gameInterface_16_.scrollPosition + gameInterface_16_.scrollHeight + -gameInterface_16_.originalHeight < i_18_)
                                                            i_18_ = -gameInterface_16_.originalHeight + gameInterface_16_.scrollHeight + -gameInterface_16_.scrollPosition;
                                                        MovedStatics.anInt2798 -= i_18_;
                                                        gameInterface_16_.scrollPosition += i_18_;
                                                    }
                                                }
                                            } else if (GameInterface.atInventoryInterfaceType == 0 || GameInterface.anInt1233 != i_7_ || gameInterface.id != PlayerAppearance.anInt704)
                                                imageRGB.drawImage(i_10_, i_11_);
                                            else
                                                imageRGB.drawImageWithOpacity(i_10_, i_11_, 128);
                                            if (imageRGB.maxWidth == 33 || gameInterface.itemAmounts[i_7_] != 1) {
                                                int i_19_ = gameInterface.itemAmounts[i_7_];
//                                                TypeFace.fontSmall.drawString(GameInterface.getShortenedAmountText(i_19_), i_12_ + 1 + i_10_, i_11_ + 10 + i_14_, 0);
                                                TypeFace.fontSmall.drawShadowedString(GameInterface.getShortenedAmountText(i_19_), i_10_ + i_12_, i_14_ + i_11_ + 9, true, 16776960);
                                            }
                                        }
                                    }
                                }
                                i_7_++;
                            }
                        }
                    } else if (gameInterface.type == GameInterfaceType.RECTANGLE) {
                        int rectangleColor;
                        if (GameInterface.checkForAlternateAction(gameInterface)) {
                            rectangleColor = gameInterface.alternateTextColor;
                            if (GameInterface.isHovering(areaId, i) && gameInterface.alternateHoveredTextColor != 0)
                                rectangleColor = gameInterface.alternateHoveredTextColor;
                        } else {
                            rectangleColor = gameInterface.textColor;
                            if (GameInterface.isHovering(areaId, i) && gameInterface.hoveredTextColor != 0)
                                rectangleColor = gameInterface.hoveredTextColor;
                        }
                        if (opacity == 0) {
                            if (!gameInterface.filled)
                                Rasterizer.drawUnfilledRectangle(absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight, rectangleColor);
                            else
                                Rasterizer.drawFilledRectangle(absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight, rectangleColor);
                        } else if (!gameInterface.filled)
                            Rasterizer.drawUnfilledRectangleAlpha(absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight, rectangleColor, -(0xff & opacity) + 256);
                        else
                            Rasterizer.drawFilledRectangleAlpha(absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight, rectangleColor, -(0xff & opacity) + 256);
                    } else if (gameInterface.type == GameInterfaceType.TEXT) {
                        TypeFace font = gameInterface.getTypeFace();
                        if (font == null) {
                            if (GameInterface.aBoolean2177)
                                result = false;
                        } else {
                            String text = gameInterface.disabledText;
                            int textColor;
                            if (GameInterface.checkForAlternateAction(gameInterface)) {
                                textColor = gameInterface.alternateTextColor;
                                if (GameInterface.isHovering(areaId, i) && gameInterface.alternateHoveredTextColor != 0)
                                    textColor = gameInterface.alternateHoveredTextColor;
                                if (gameInterface.alternateText.length() > 0)
                                    text = gameInterface.alternateText;
                            } else {
                                textColor = gameInterface.textColor;
                                if (GameInterface.isHovering(areaId, i) && gameInterface.hoveredTextColor != 0)
                                    textColor = gameInterface.hoveredTextColor;
                            }
                            if (gameInterface.isNewInterfaceFormat && gameInterface.itemId != -1) {
                                ItemDefinition itemDefinition = ItemDefinition.forId(gameInterface.itemId, 10);
                                text = itemDefinition.name;
                                if (text == null)
                                    text = "null";
                                if (itemDefinition.stackable == 1 || gameInterface.itemAmount != 1)
                                    text = text + Native.amountPrefixX + MovedStatics.method903(gameInterface.itemAmount);
                            }
                            if (gameInterface.actionType == 6 && MovedStatics.lastContinueTextWidgetId == gameInterface.id) {
                                textColor = gameInterface.textColor;
                                text = English.pleaseWait;
                            }
                            if (Rasterizer.destinationWidth == 479) {
                                if (textColor == 16776960)
                                    textColor = 255;
                                if (textColor == 49152)
                                    textColor = 16777215;
                            }

                            text = MovedStatics.method532(gameInterface, text);
                            font.drawText(text, absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight, textColor, gameInterface.textShadowed, gameInterface.xTextAlignment, gameInterface.yTextAlignment, gameInterface.lineHeight);
                        }
                    } else if (gameInterface.type == GameInterfaceType.GRAPHIC) {
                        if (gameInterface.isNewInterfaceFormat) {
                            int maxWidth = 0;
                            int maxHeight = 0;
                            ImageRGB spriteRgb;

                            if (gameInterface.itemId == -1)
                                spriteRgb = gameInterface.getImageRgb(false);
                            else {
                                spriteRgb = ItemDefinition.sprite(gameInterface.itemAmount, gameInterface.itemId, 0);
                                maxWidth = spriteRgb.maxWidth;
                                maxHeight = spriteRgb.maxHeight;

                                // TODO find out why this is done
                                spriteRgb.maxHeight = 32;
                                spriteRgb.maxWidth = 32;
                            }

                            if (spriteRgb != null) {
                                int spriteHeight = spriteRgb.imageHeight;
                                int spriteWidth = spriteRgb.imageWidth;

                                if (gameInterface.tiled) {
                                    int[] viewportDimensions = new int[4];
                                    Rasterizer.getViewportDimensions(viewportDimensions);

                                    // Cap sprite to viewport dimensions
                                    int spriteTopX = absoluteX;
                                    if (viewportDimensions[0] > spriteTopX)
                                        spriteTopX = viewportDimensions[0];
                                    int spriteTopY = absoluteY;
                                    if (viewportDimensions[1] > spriteTopY)
                                        spriteTopY = viewportDimensions[1];
                                    int spriteBottomX = gameInterface.originalWidth + absoluteX;
                                    if (viewportDimensions[2] < spriteBottomX)
                                        spriteBottomX = viewportDimensions[2];
                                    int spriteBottomY = gameInterface.originalHeight + absoluteY;
                                    if (spriteBottomY > viewportDimensions[3])
                                        spriteBottomY = viewportDimensions[3];

                                    Rasterizer.setBounds(spriteTopX, spriteTopY, spriteBottomX, spriteBottomY);
                                    int i_31_ = (gameInterface.originalWidth - (1 + -spriteWidth)) / spriteWidth;
                                    int i_32_ = (gameInterface.originalHeight - (1 + -spriteHeight)) / spriteHeight;
                                    for (int row = 0; i_31_ > row; row++) {
                                        for (int col = 0; i_32_ > col; col++) {
                                            if (gameInterface.textureId == 0) {
                                                if (opacity == 0)
                                                    spriteRgb.drawImage(row * spriteWidth + absoluteX, col * spriteHeight + absoluteY);
                                                else
                                                    spriteRgb.drawImageWithOpacity(absoluteX + row * spriteWidth, absoluteY + spriteHeight * col, -(opacity & 0xff) + 256);
                                            } else
                                                spriteRgb.drawImageWithTexture(spriteWidth / 2 + row * spriteWidth + absoluteX, spriteHeight / 2 + absoluteY + spriteHeight * col, gameInterface.textureId, 4096);
                                        }
                                    }
                                    Rasterizer.setViewportDimensions(viewportDimensions);
                                } else {
                                    int i_26_ = 4096 * gameInterface.originalWidth / spriteWidth;
                                    if (gameInterface.textureId == 0) {
                                        if (opacity == 0) {
                                            if (gameInterface.originalWidth == spriteWidth && gameInterface.originalHeight == spriteHeight)
                                                spriteRgb.drawImage(absoluteX, absoluteY);
                                            else
                                                spriteRgb.method732(absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight);
                                        } else
                                            spriteRgb.method716(absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight, 256 + -(0xff & opacity));
                                    } else
                                        spriteRgb.drawImageWithTexture(gameInterface.originalWidth / 2 + absoluteX, gameInterface.originalHeight / 2 + absoluteY, gameInterface.textureId, i_26_);
                                }
                            } else if (GameInterface.aBoolean2177)
                                result = false;
                            if (gameInterface.itemId != -1) {
                                // TODO find out why this renders when maxWidth == 33
                                if (gameInterface.itemAmount != 1 || maxWidth == 33) {
                                    TypeFace.fontSmall.drawString(Integer.toString(gameInterface.itemAmount), absoluteX + 1, absoluteY + 10, 0);
                                    TypeFace.fontSmall.drawString(Integer.toString(gameInterface.itemAmount), absoluteX, 9 + absoluteY, 16776960);
                                }
                                spriteRgb.maxWidth = maxWidth;
                                spriteRgb.maxHeight = maxHeight;
                            }
                        } else {
                            ImageRGB imageRGB = gameInterface.getImageRgb(GameInterface.checkForAlternateAction(gameInterface));
                            if (imageRGB != null)
                                imageRGB.drawImage(absoluteX, absoluteY);
                            else if (GameInterface.aBoolean2177)
                                result = false;
                        }
                    } else if (gameInterface.type == GameInterfaceType.MODEL) {
                        boolean applyAlternateAction = GameInterface.checkForAlternateAction(gameInterface);
                        int animationId;
                        if (!applyAlternateAction)
                            animationId = gameInterface.animation;
                        else
                            animationId = gameInterface.alternateAnimation;

                        Model model;
                        if (gameInterface.modelType != InterfaceModelType.PLAYER) {
                            if (animationId == -1) {
                                model = gameInterface.getModelForInterface(null, -1, applyAlternateAction, Player.localPlayer.playerAppearance);
                            } else {
                                AnimationSequence animationSequence = ProducingGraphicsBuffer_Sub1.getAnimationSequence(animationId);
                                model = gameInterface.getModelForInterface(animationSequence, gameInterface.animationFrame, applyAlternateAction, Player.localPlayer.playerAppearance);
                            }
                            // TODO FramemapDefinition.aBoolean2177 might be object/model/sprite doesnt exist
                            if (model == null && GameInterface.aBoolean2177)
                                result = false;
                        } else if (gameInterface.modelId != 0) {
                            model = Player.localPlayer.getRotatedModel();
                        } else {
                            model = Player.activePlayerAppearance.getAnimatedModel(null, null, -1, -1);
                        }

                        int rotationX = gameInterface.rotationX;
                        int rotationY = gameInterface.rotationY;
                        int offsetY2d = gameInterface.offsetY2d;
                        int rotationZ = gameInterface.rotationZ;
                        int offsetX2d = gameInterface.offsetX2d;
                        int modelZoom = gameInterface.modelZoom;
                        if (gameInterface.itemId != -1) {
                            ItemDefinition itemDefinition = ItemDefinition.forId(gameInterface.itemId, 10);
                            itemDefinition = itemDefinition.method743(gameInterface.itemAmount);
                            model = itemDefinition.asGroundStack(true, 1);
                            rotationY = itemDefinition.zan2d;
                            offsetY2d = itemDefinition.yOffset2d;
                            offsetX2d = itemDefinition.xOffset2d;
                            rotationX = itemDefinition.xan2d;
                            modelZoom = itemDefinition.zoom2d;
                            rotationZ = itemDefinition.yan2d;
                            if (gameInterface.originalWidth > 0)
                                modelZoom = 32 * modelZoom / gameInterface.originalWidth;
                        }
                        Rasterizer3D.setBounds(absoluteX + gameInterface.originalWidth / 2, gameInterface.originalHeight / 2 + absoluteY);

                        int camHeight = modelZoom * Rasterizer3D.sinetable[rotationX] >> 16;
                        int camDistance = modelZoom * Rasterizer3D.cosinetable[rotationX] >> 16;
                        if (model != null) {
                            if (gameInterface.isNewInterfaceFormat) {
                                model.method799();
                                // For some reason, drawOrthogonalModel does the same thing as drawModel
                                if (gameInterface.orthogonal)
                                    model.drawOrthogonalModel(0, rotationZ, rotationY, rotationX, offsetX2d, offsetY2d + camHeight + model.modelHeight / 2, camDistance + offsetY2d, modelZoom);
                                else
                                    model.drawModel(0, rotationZ, rotationY, rotationX, offsetX2d, offsetY2d + model.modelHeight / 2 + camHeight, camDistance + offsetY2d);
                            } else {
                                model.drawModel(0, rotationZ, 0, rotationX, 0, camHeight, camDistance);
                            }
                        }
                        Rasterizer3D.resetBoundsTo3dViewport();
                    } else {
                        if (gameInterface.type == GameInterfaceType.TEXT_INVENTORY) {
                            TypeFace font = gameInterface.getTypeFace();
                            if (font == null) {
                                if (GameInterface.aBoolean2177)
                                    result = false;
                                continue;
                            }
                            int itemSlot = 0;
                            for (int row = 0; row < gameInterface.originalHeight; row++) {
                                for (int col = 0; col < gameInterface.originalWidth; col++) {
                                    if (gameInterface.items[itemSlot] > 0) {
                                        ItemDefinition itemDefinition = ItemDefinition.forId(-1 + gameInterface.items[itemSlot], 10);
                                        String itemName = itemDefinition.name;
                                        if (itemName == null)
                                            itemName = "null";
                                        if (itemDefinition.stackable == 1 || gameInterface.itemAmounts[itemSlot] != 1)
                                            itemName = itemName + Native.amountPrefixX + MovedStatics.method903(gameInterface.itemAmounts[itemSlot]);
                                        int itemX = col * (gameInterface.itemSpritePadsX + 115) + absoluteX;
                                        int itemY = row * (gameInterface.itemSpritePadsY + 12) + absoluteY;
                                        if (gameInterface.xTextAlignment == 0)
                                            font.drawShadowedString(itemName, itemX, itemY, gameInterface.textShadowed, gameInterface.textColor);
                                        else if (gameInterface.xTextAlignment == 1)
                                            font.drawShadowedStringCenter(itemName, itemX + gameInterface.originalWidth / 2, itemY, gameInterface.textColor, gameInterface.textShadowed);
                                        else
                                            font.drawShadowedStringRight(itemName, -1 + gameInterface.originalWidth + itemX, itemY, gameInterface.textColor, gameInterface.textShadowed);
                                    }
                                    itemSlot++;
                                }
                            }
                        }
                        if (gameInterface.type == GameInterfaceType.IF1_TOOLTIP && MovedStatics.method438(areaId, i) && RSString.tooltipDelay == MovedStatics.durationHoveredOverWidget) {
                            int textWidth = 0;
                            int textHeight = 0;
                            TypeFace class40_sub5_sub14_sub1 = MovedStatics.fontNormal;
                            String text = gameInterface.disabledText;
                            text = MovedStatics.method532(gameInterface, text);
                            while (text.length() > 0) {
                                int lineBreakCharacter = text.indexOf(Native.lineBreak);
                                String textLine;
                                if (lineBreakCharacter == -1) {
                                    // Not a multiline text
                                    textLine = text;
                                    text = "";
                                } else {
                                    // Multiline text
                                    textLine = text.substring(0, lineBreakCharacter);
                                    text = text.substring(2 + lineBreakCharacter);
                                }
                                int lineWidth = class40_sub5_sub14_sub1.getTextDisplayedWidth(textLine);
                                textHeight += class40_sub5_sub14_sub1.characterDefaultHeight + 1;
                                if (textWidth < lineWidth)
                                    textWidth = lineWidth;
                            }
                            textHeight += 7;
                            int tooltipY = 5 + gameInterface.originalHeight + absoluteY;
                            if (tooltipY + textHeight > maxY)
                                tooltipY = maxY + -textHeight;
                            textWidth += 6;
                            int tooltipX = -5 + gameInterface.originalWidth + absoluteX - textWidth;
                            if (tooltipX < 5 + absoluteX)
                                tooltipX = 5 + absoluteX;
                            if (textWidth + tooltipX > maxX)
                                tooltipX = -textWidth + maxX;
                            Rasterizer.drawFilledRectangle(tooltipX, tooltipY, textWidth, textHeight, 16777120);
                            Rasterizer.drawUnfilledRectangle(tooltipX, tooltipY, textWidth, textHeight, 0);
                            text = gameInterface.disabledText;
                            int tooltipTitleY = 2 + tooltipY + class40_sub5_sub14_sub1.characterDefaultHeight;
                            text = MovedStatics.method532(gameInterface, text);
                            while (text.length() > 0) {
                                int lineBreakCharacter = text.indexOf(Native.lineBreak);
                                String textLine;
                                if (lineBreakCharacter == -1) {
                                    textLine = text;
                                    text = "";
                                } else {
                                    textLine = text.substring(0, lineBreakCharacter);
                                    text = text.substring(lineBreakCharacter + 2);
                                }
                                class40_sub5_sub14_sub1.drawShadowedString(textLine, tooltipX + 3, tooltipTitleY, false, 0);
                                tooltipTitleY += 1 + class40_sub5_sub14_sub1.characterDefaultHeight;
                            }
                        }
                        if (gameInterface.type == GameInterfaceType.LINE)
                            Rasterizer.drawDiagonalLine(absoluteX, absoluteY, gameInterface.originalWidth + absoluteX, gameInterface.originalHeight + absoluteY, gameInterface.textColor);
                    }

                    // Draw debug information for non layer widgets and non tooltip widgets
                    if (Configuration.DEBUG_WIDGETS && gameInterface.type != GameInterfaceType.LAYER && gameInterface.type != GameInterfaceType.IF1_TOOLTIP && MovedStatics.hoveredWidgetId == gameInterface.id) {
                        Rasterizer.drawUnfilledRectangle(absoluteX, absoluteY, gameInterface.originalWidth, gameInterface.originalHeight, 0xffff00);
                    }
                }
            }
        }
        return result;
    }

    public static void renderFlames() {
        if (MovedStatics.anInt2452 <= 0) {
            if (MovedStatics.anInt2613 > 0) {
                for (int i = 0; i < 256; i++) {
                    if (MovedStatics.anInt2613 > 768)
                        MovedStatics.anIntArray1013[i] = MovedStatics.method614(MovedStatics.anIntArray1198[i], MovedStatics.anIntArray3248[i], -MovedStatics.anInt2613 + 1024);
                    else if (MovedStatics.anInt2613 > 256)
                        MovedStatics.anIntArray1013[i] = MovedStatics.anIntArray3248[i];
                    else
                        MovedStatics.anIntArray1013[i] = MovedStatics.method614(MovedStatics.anIntArray3248[i], MovedStatics.anIntArray1198[i], -MovedStatics.anInt2613 + 256);
                }
            } else {
                System.arraycopy(MovedStatics.anIntArray1198, 0, MovedStatics.anIntArray1013, 0, 256);
            }
        } else {
            for (int i = 0; i < 256; i++) {
                if (MovedStatics.anInt2452 <= 768) {
                    if (MovedStatics.anInt2452 > 256)
                        MovedStatics.anIntArray1013[i] = Renderable.anIntArray2865[i];
                    else
                        MovedStatics.anIntArray1013[i] = MovedStatics.method614(Renderable.anIntArray2865[i], MovedStatics.anIntArray1198[i], -MovedStatics.anInt2452 + 256);
                } else
                    MovedStatics.anIntArray1013[i] = MovedStatics.method614(MovedStatics.anIntArray1198[i], Renderable.anIntArray2865[i], -MovedStatics.anInt2452 + 1024);
            }
        }
        int i = 256;
        System.arraycopy(MovedStatics.aClass40_Sub5_Sub14_Sub4_918.pixels, 0, MovedStatics.flameLeftBackground.pixels, 0, 33920);
        int i_61_ = 0;
        int i_62_ = 1152;
        for (int i_63_ = 1; i - 1 > i_63_; i_63_++) {
            int i_64_ = (-i_63_ + i) * MovedStatics.anIntArray466[i_63_] / i;
            int i_65_ = i_64_ + 22;
            if (i_65_ < 0)
                i_65_ = 0;
            i_61_ += i_65_;
            for (int i_66_ = i_65_; i_66_ < 128; i_66_++) {
                int i_67_ = MovedStatics.anIntArray178[i_61_++];
                if (i_67_ != 0) {
                    int i_68_ = -i_67_ + 256;
                    int i_69_ = i_67_;
                    i_67_ = MovedStatics.anIntArray1013[i_67_];
                    int i_70_ = MovedStatics.flameLeftBackground.pixels[i_62_];
                    MovedStatics.flameLeftBackground.pixels[i_62_++] = BitUtils.bitWiseAND(-16711936, BitUtils.bitWiseAND(i_67_, 16711935) * i_69_ + i_68_ * BitUtils.bitWiseAND(i_70_, 16711935)) + BitUtils.bitWiseAND(BitUtils.bitWiseAND(65280, i_70_) * i_68_ + i_69_ * BitUtils.bitWiseAND(65280, i_67_), 16711680) >> 8;
                } else
                    i_62_++;
            }
            i_62_ += i_65_;
        }
        i_62_ = 1176;
        i_61_ = 0;

        for (int i_71_ = 0; i_71_ < 33920; i_71_++)
            GameObject.flameRightBackground.pixels[i_71_] = MovedStatics.aClass40_Sub5_Sub14_Sub4_2043.pixels[i_71_];
        for (int i_72_ = 1; i_72_ < -1 + i; i_72_++) {
            int i_73_ = (-i_72_ + i) * MovedStatics.anIntArray466[i_72_] / i;
            int i_74_ = 103 + -i_73_;
            i_62_ += i_73_;
            for (int i_75_ = 0; i_75_ < i_74_; i_75_++) {
                int i_76_ = MovedStatics.anIntArray178[i_61_++];
                if (i_76_ != 0) {
                    int i_77_ = i_76_;
                    int i_78_ = GameObject.flameRightBackground.pixels[i_62_];
                    int i_79_ = 256 + -i_76_;
                    i_76_ = MovedStatics.anIntArray1013[i_76_];
                    GameObject.flameRightBackground.pixels[i_62_++] = BitUtils.bitWiseAND(i_77_ * BitUtils.bitWiseAND(65280, i_76_) + i_79_ * BitUtils.bitWiseAND(65280, i_78_), 16711680) + BitUtils.bitWiseAND(i_79_ * BitUtils.bitWiseAND(16711935, i_78_) + BitUtils.bitWiseAND(16711935, i_76_) * i_77_, -16711936) >> 8;
                } else
                    i_62_++;
            }
            i_62_ += 128 - (i_74_ + i_73_);
            i_61_ += -i_74_ + 128;
        }
    }


    public static void setConfigToDefaults() {
        aLong1203 = 0L;
        mouseCapturer.coord = 0;
        duplicateClickCount = 0;
        aBoolean1735 = true;
        MovedStatics.aBoolean571 = true;
        MovedStatics.method540();
        IncomingPackets.secondLastOpcode = -1;
        MovedStatics.menuOpen = false;
        IncomingPackets.lastOpcode = -1;
        IncomingPackets.opcode = -1;
        MovedStatics.systemUpdateTime = 0;
        IncomingPackets.cyclesSinceLastPacket = 0;
        Player.headIconDrawType = 0;
        OutgoingPackets.buffer.currentPosition = 0;
        SceneCluster.idleLogout = 0;
        IncomingPackets.thirdLastOpcode = -1;
        IncomingPackets.incomingPacketBuffer.currentPosition = 0;
        MovedStatics.menuActionRow = 0;
        MovedStatics.method650(0);
        for (int i = 0; i < 100; i++)
            ChatBox.chatMessages[i] = null;
        MovedStatics.itemSelected = 0;
        MovedStatics.destinationX = 0;
        Buffer.anInt1985 = -1;
        Player.npcCount = 0;
        SoundSystem.reset();
        widgetSelected = 0;
        // TODO is this necessary? or should it be removed alongside other randomisation
        Game.playerCamera.setYaw(0x7ff & -10 + (int) (20.0 * Math.random()));
        Minimap.minimapState = 0;
        Player.localPlayerCount = 0;
        destinationY = 0;
        for (int i = 0; i < 2048; i++) {
            Player.trackedPlayers[i] = null;
            Player.trackedPlayerAppearanceCache[i] = null;
        }
        for (int i = 0; i < 32768; i++)
            Player.npcs[i] = null;
        Player.localPlayer = Player.trackedPlayers[2047] = new Player();
        MovedStatics.projectileQueue.clear();
        MovedStatics.spotAnimQueue.clear();
        for (int i = 0; i < 4; i++) {
            for (int i_82_ = 0; i_82_ < 104; i_82_++) {
                for (int i_83_ = 0; i_83_ < 104; i_83_++)
                    MovedStatics.groundItems[i][i_82_][i_83_] = null;
            }
        }
        MovedStatics.aLinkedList_1064 = new LinkedList();
        Player.friendsCount = 0;
        Player.friendListStatus = 0;
        GameInterface.resetInterface(ChatBox.dialogueId);
        ChatBox.dialogueId = -1;
        GameInterface.resetInterface(GameInterface.chatboxInterfaceId);
        GameInterface.chatboxInterfaceId = -1;
        GameInterface.resetInterface(GameInterface.gameScreenInterfaceId);
        GameInterface.gameScreenInterfaceId = -1;
        GameInterface.resetInterface(GameInterface.fullscreenInterfaceId);
        GameInterface.fullscreenInterfaceId = -1;
        GameInterface.resetInterface(GameInterface.fullscreenSiblingInterfaceId);
        GameInterface.fullscreenSiblingInterfaceId = -1;
        GameInterface.resetInterface(GameInterface.tabAreaInterfaceId);
        GameInterface.tabAreaInterfaceId = -1;
        GameInterface.resetInterface(GroundItemTile.walkableWidgetId);
        ChatBox.inputType = 0;
        ChatBox.messagePromptRaised = false;
        MovedStatics.menuOpen = false;
        GroundItemTile.walkableWidgetId = -1;
        Native.clickToContinueString = null;
        MovedStatics.lastContinueTextWidgetId = -1;
        Player.flashingTabId = -1;
        MovedStatics.multiCombatState = 0;
        Player.currentTabId = 3;
        Player.activePlayerAppearance.setPlayerAppearance(null, false, new int[5], -1);
        for (int i = 0; i < 5; i++) {
            Player.playerActions[i] = null;
            Player.playerActionsLowPriority[i] = false;
        }
        aBoolean519 = true;
    }

    public static void method353() {
        MovedStatics.anInt2628++;
        renderPlayers(0, true);
        renderNPCs(true);
        renderPlayers(0, false);
        renderNPCs(false);
        MovedStatics.renderProjectiles();
        MovedStatics.renderSpotAnims();
        if(!Player.cutsceneActive) {
            int pitch = Game.playerCamera.getPitch();
            if(SceneCamera.cameraTerrainMinScaledPitch / 256 > pitch) {
                pitch = SceneCamera.cameraTerrainMinScaledPitch / 256;
            }

            if(SceneCamera.customCameraActive[4] && 128 + SceneCamera.customCameraAmplitude[4] > pitch) {
                pitch = 128 + SceneCamera.customCameraAmplitude[4];
            }

            Game.playerCamera.setPitch(pitch);
        }

        int i;
        if(!Player.cutsceneActive) {
            i = MovedStatics.method764();
        } else {
            i = MovedStatics.method546();
        }

        Camera activeCamera = getActiveCamera();

        Point3d shakeOffsetPosition = new Point3d(0, 0, 0);
        CameraRotation shakeOffsetRotation = new CameraRotation(0, 0);

        for(int cameraType = 0; cameraType < 5; cameraType++) {
            if(SceneCamera.customCameraActive[cameraType]) {
                int shakeAmount = (int) ((double) (SceneCamera.customCameraJitter[cameraType] * 2 + 1) * Math.random() - (double) SceneCamera.customCameraJitter[cameraType] + Math.sin((double) SceneCamera.customCameraTimer[cameraType] * ((double) SceneCamera.customCameraFrequency[cameraType] / 100.0)) * (double) SceneCamera.customCameraAmplitude[cameraType]);
                if(cameraType == 1) {
                    shakeOffsetPosition = shakeOffsetPosition.addZ(shakeAmount);
                }
                if(cameraType == 0) {
                    shakeOffsetPosition = shakeOffsetPosition.addX(shakeAmount);
                }
                if(cameraType == 2) {
                    shakeOffsetPosition = shakeOffsetPosition.addY(shakeAmount);
                }
                if(cameraType == 4) {
                    shakeOffsetRotation = shakeOffsetRotation.addPitch(shakeAmount);
                }
                if(cameraType == 3) {
                    shakeOffsetRotation = shakeOffsetRotation.addYaw(shakeAmount);
                }
            }
        }

        activeCamera.setOffsetPosition(shakeOffsetPosition);
        activeCamera.setOffsetRotation(shakeOffsetRotation);

        Class65.method1018();
        MouseHandler.cursorY = MouseHandler.mouseY - 4;
        MouseHandler.gameScreenClickable = true;
        MouseHandler.cursorX = MouseHandler.mouseX - 4;
        Model.resourceCount = 0;
        Rasterizer.resetPixels();

        currentScene.render(activeCamera, i);
        currentScene.clearInteractiveObjectCache();
        MovedStatics.draw2DActorAttachments();
        MovedStatics.drawPositionHintIcon();
        ((Class35) Rasterizer3D.interface3).animateTextures(MovedStatics.anInt199);
        MovedStatics.draw3dScreen();

        DebugTools.drawWalkPath();
        DebugTools.drawClipping();

        if(ScreenController.frameMode == ScreenMode.FIXED) {
            Console.console.drawConsole(512, 334);
            Console.console.drawConsoleArea(512, 334);
        } else {
            ScreenController.RenderResizableUI();
            Console.console.drawConsole(ScreenController.drawWidth, 334);
            Console.console.drawConsoleArea(ScreenController.drawWidth, 334);
        }


        if(aBoolean519 && UpdateServer.getActiveCount(false, true) == 0) {
            aBoolean519 = false;
        }
        if(aBoolean519) {
            Class65.method1018();
            Rasterizer.resetPixels();
            MovedStatics.method940(English.loadingPleaseWait, false, null);
        }

        Player.drawGameScreenGraphics();
    }

    /**
     * Get the currently active camera.
     */
    public static Camera getActiveCamera() {
        return Player.cutsceneActive ? Game.cutsceneCamera : Game.playerCamera;
    }

    public static void method357(CacheArchive arg0, CacheArchive arg2) {
        GroundItemTile.aCacheArchive_1375 = arg2;
        ActorDefinition.count = GroundItemTile.aCacheArchive_1375.fileLength(9);

        MovedStatics.aCacheArchive_1577 = arg0;
    }

    public static IndexedImage method359(String arg0, String arg1, CacheArchive arg2) {
        int i = arg2.getHash(arg0);
        int i_23_ = arg2.method179(i, arg1);
        return method363(arg2, i_23_, i);
    }

    public static IndexedImage method363(CacheArchive arg0, int arg2, int arg3) {
        if(!ImageRGB.spriteExists(arg2, arg3, arg0)) {
            return null;
        }
        return MovedStatics.method538();

    }

    public static void drawGameScreen() {
        if(MovedStatics.clearScreen) {
            MovedStatics.clearScreen = false;
            MovedStatics.drawWelcomeScreenGraphics();
            GameInterface.drawTabIcons = true;
            ChatBox.redrawChatbox = true;
            GameInterface.redrawTabArea = true;
            MovedStatics.redrawChatbox = true;
            drawCount++;
        } else if(drawCount != 0) {
            MovedStatics.method763(MouseHandler.gameCanvas, CacheArchive.gameImageCacheArchive);
        }
        if(GameInterface.chatboxInterfaceId == -1) {
            chatboxInterface.scrollPosition = -77 + -ChatBox.chatboxScroll + ChatBox.chatboxScrollMax;
            if(MouseHandler.mouseX > 448 && MouseHandler.mouseX < 560 && MouseHandler.mouseY > 332) {
                GameInterface.scrollInterface(77, MouseHandler.mouseY + -357, -17 + MouseHandler.mouseX, ChatBox.chatboxScrollMax, chatboxInterface, 463, -1, 0);
            }
            int currentScroll = ChatBox.chatboxScrollMax - 77 - chatboxInterface.scrollPosition;
            if(currentScroll < 0) {
                currentScroll = 0;
            }
            if(currentScroll > ChatBox.chatboxScrollMax + -77) {
                currentScroll = -77 + ChatBox.chatboxScrollMax;
            }
            if(currentScroll != ChatBox.chatboxScroll) {
                ChatBox.chatboxScroll = currentScroll;
                ChatBox.redrawChatbox = true;
            }
        }
        if(GameInterface.chatboxInterfaceId == -1 && ChatBox.inputType == 3) {
            chatboxInterface.scrollPosition = ChatBox.itemSearchScroll;
            int scrollMax = 14 * ChatBox.itemSearchResultCount + 7;
            if(MouseHandler.mouseX > 448 && MouseHandler.mouseX < 560 && MouseHandler.mouseY > 332) {
                GameInterface.scrollInterface(77, MouseHandler.mouseY - 357, -17 + MouseHandler.mouseX, scrollMax, chatboxInterface, 463, -1, 0);
            }
            int currentScroll = chatboxInterface.scrollPosition;
            if(currentScroll < 0) {
                currentScroll = 0;
            }
            if(currentScroll > scrollMax - 77) {
                currentScroll = scrollMax - 77;
            }
            if(currentScroll != ChatBox.itemSearchScroll) {
                ChatBox.itemSearchScroll = currentScroll;
                ChatBox.redrawChatbox = true;
            }
        }
        if(ScreenController.frameMode == ScreenMode.FIXED) {

            if(MovedStatics.menuOpen && MovedStatics.menuScreenArea == 1) {
                GameInterface.redrawTabArea = true;
            }
            if(GameInterface.tabAreaInterfaceId != -1) {
                boolean bool = Renderable.handleSequences(GameInterface.tabAreaInterfaceId);
                if(bool) {
                    GameInterface.redrawTabArea = true;
                }
            }
            method353();

            if(GameInterface.atInventoryInterfaceType == -3) {
                GameInterface.redrawTabArea = true;
            }
            if(MovedStatics.activeInterfaceType == 2) {
                GameInterface.redrawTabArea = true;
            }
            MovedStatics.drawTabArea();

            if(GameInterface.chatboxInterfaceId != -1) {
                boolean bool = Renderable.handleSequences(GameInterface.chatboxInterfaceId);
                if(bool) {
                    ChatBox.redrawChatbox = true;
                }
            }
            if(ChatBox.dialogueId != -1) {
                boolean bool = Renderable.handleSequences(ChatBox.dialogueId);
                if(bool) {
                    ChatBox.redrawChatbox = true;
                }
            }
            if(GameInterface.atInventoryInterfaceType == 3) {
                ChatBox.redrawChatbox = true;
            }
            if(MovedStatics.activeInterfaceType == 3) {
                ChatBox.redrawChatbox = true;
            }
            if(Native.clickToContinueString != null) {
                ChatBox.redrawChatbox = true;
            }
            if(MovedStatics.menuOpen && MovedStatics.menuScreenArea == 2) {
                ChatBox.redrawChatbox = true;
            }
            if(ChatBox.redrawChatbox) {
                ChatBox.redrawChatbox = false;
                ChatBox.renderChatbox();
                //            Console.console.drawConsoleArea();
            }

            Minimap.renderMinimap();


            if(Player.flashingTabId != -1) {
                GameInterface.drawTabIcons = true;
            }
            if(GameInterface.drawTabIcons) {
                if(Player.flashingTabId != -1 && Player.flashingTabId == Player.currentTabId) {
                    Player.flashingTabId = -1;
                    OutgoingPackets.sendMessage(new ClickFlashingTabIconOutboundMessage(Player.currentTabId));
                }
                GameInterface.drawTabIcons = false;
                MovedStatics.showIconsRedrawnText = true;
                MovedStatics.method527(Player.currentTabId, Player.tabWidgetIds, GameInterface.tabAreaInterfaceId == -1, MovedStatics.pulseCycle % 20 >= 10 ? Player.flashingTabId : -1);
            }
            if(MovedStatics.redrawChatbox) {
                MovedStatics.showIconsRedrawnText = true;
                MovedStatics.redrawChatbox = false;
                method943(ChatBox.tradeMode, MovedStatics.fontNormal, ChatBox.privateChatMode, ChatBox.publicChatMode);
            }

            SoundSystem.updateObjectSounds(Player.localPlayer.worldX, Player.worldLevel, MovedStatics.anInt199, Player.localPlayer.worldY);
            MovedStatics.anInt199 = 0;

        } else {


            if(GameInterface.tabAreaInterfaceId != -1) {
                Renderable.handleSequences(GameInterface.tabAreaInterfaceId);
            }

            if(GameInterface.chatboxInterfaceId != -1) {
                Renderable.handleSequences(GameInterface.chatboxInterfaceId);
            }

            if(ChatBox.dialogueId != -1) {
                Renderable.handleSequences(ChatBox.dialogueId);
            }
            method353();
            ChatBox.renderChatbox();

            MovedStatics.drawTabArea();

            Minimap.renderMinimap();


            if(Player.flashingTabId != -1) {
                GameInterface.drawTabIcons = true;
            }
            if(GameInterface.drawTabIcons) {
                if(Player.flashingTabId != -1 && Player.flashingTabId == Player.currentTabId) {
                    Player.flashingTabId = -1;
                    OutgoingPackets.sendMessage(new ClickFlashingTabIconOutboundMessage(Player.currentTabId));
                }
                GameInterface.drawTabIcons = false;
                MovedStatics.showIconsRedrawnText = true;
                MovedStatics.method527(Player.currentTabId, Player.tabWidgetIds, GameInterface.tabAreaInterfaceId == -1, MovedStatics.pulseCycle % 20 >= 10 ? Player.flashingTabId : -1);
            }
            if(MovedStatics.redrawChatbox) {
                MovedStatics.showIconsRedrawnText = true;
                MovedStatics.redrawChatbox = false;
                method943(ChatBox.tradeMode, MovedStatics.fontNormal, ChatBox.privateChatMode, ChatBox.publicChatMode);
            }

            SoundSystem.updateObjectSounds(Player.localPlayer.worldX, Player.worldLevel, MovedStatics.anInt199, Player.localPlayer.worldY);
            MovedStatics.anInt199 = 0;
        }

    }

    public static void displayMessageForResponseCode(int responseCode) {
        if(responseCode == -3) {
            Class60.setLoginScreenMessage(English.connectionTimedOut, English.pleaseTryUsingDifferentWorld, "");
        } else if(responseCode == -2) {
            Class60.setLoginScreenMessage("", English.errorConnectingToServer, "");
        } else if(responseCode == -1) {
            Class60.setLoginScreenMessage(English.noResponseFromServer, English.pleaseTryUsingDifferentWorld, "");
        } else if(responseCode == 3) {
            Class60.setLoginScreenMessage("", English.invalidUsernameOrPassword, "");
        } else if(responseCode == 4) {
            Class60.setLoginScreenMessage(English.yourAccountHasBeenDisabled, English.pleaseCheckYourMessageCenterForDetails, "");
        } else if(responseCode == 5) {
            Class60.setLoginScreenMessage(English.yourAccountIsAlreadyLoggedIn, English.tryAgainIn60Secs, "");
        } else if(responseCode == 6) {
            Class60.setLoginScreenMessage(English.gameHasBeenUpdated, English.pleaseReloadThisPage, "");
        } else if(responseCode == 7) {
            Class60.setLoginScreenMessage(English.theWorldIsFull, English.pleaseUseADifferentWorld, "");
        } else if(responseCode == 8) {
            Class60.setLoginScreenMessage(English.unableToConnect, English.loginServerOffline, "");
        } else if(responseCode == 9) {
            Class60.setLoginScreenMessage(English.loginLimitExceeded, English.tooManyConnectionsFromYourAddress, "");
        } else if(responseCode == 10) {
            Class60.setLoginScreenMessage(English.unableToConnect, English.badSessionId, "");
        } else if(responseCode == 11) {
            Class60.setLoginScreenMessage(English.weSuspectSomeoneKnowsYourPassword, English.pressChangeYourPasswordOnFrontPage, "");
        } else if(responseCode == 12) {
            Class60.setLoginScreenMessage(English.youNeedMembersAccountToLoginToThisWorld, English.pleaseSubscribeOrUseDifferentWorld, "");
        } else if(responseCode == 13) {
            Class60.setLoginScreenMessage(English.couldNotCompleteLogin, English.pleaseTryUsingDifferentWorld, "");
        } else if(responseCode == 14) {
            Class60.setLoginScreenMessage(English.theServerIsBeingUpdated, English.pleaseWait1MinuteAndTryAgain, "");
        } else if(responseCode == 16) {
            Class60.setLoginScreenMessage(English.tooManyIncorrectLoginsFromYourAddress, English.pleaseWait5MinutesBeforeTryingAgain, "");
        } else if(responseCode == 17) {
            Class60.setLoginScreenMessage(English.youAreStandingInMembersOnlyArea, English.toPlayOnThisWorldMoveToFreeArea, "");
        } else if(responseCode == 18) {
            Class60.setLoginScreenMessage(English.accountLockedAsWeSuspectItHasBeenStolen, English.pressRecoverLockedAccountOnFrontPage, "");
        } else if(responseCode == 20) {
            Class60.setLoginScreenMessage(English.invalidLoginserverRequested, English.pleaseTryUsingDifferentWorld, "");
        } else if(responseCode == 22) {
            Class60.setLoginScreenMessage(English.malformedLoginPacket, English.pleaseTryAgain, "");
        } else if(responseCode == 23) {
            Class60.setLoginScreenMessage(English.noReplyFromLoginserver, English.pleaseWait1MinuteAndTryAgain, "");
        } else if(responseCode == 24) {
            Class60.setLoginScreenMessage(English.errorLoadingYourProfile, English.pleaseContactCustomerSupport, "");
        } else if(responseCode == 25) {
            Class60.setLoginScreenMessage(English.unexpectedLoginserverResponse, English.pleaseTryUsingDifferentWorld, "");
        } else if(responseCode == 26) {
            Class60.setLoginScreenMessage(English.thisComputersAddressHasBeenBlocked, English.asItWasUsedToBreakOurRules, "");
        } else if(responseCode == 27) {
            Class60.setLoginScreenMessage("", English.serviceUnavailable, "");
        } else {
            Class60.setLoginScreenMessage(English.unexpectedServerResponse, English.pleaseTryUsingDifferentWorld, "");
        }
        MovedStatics.processGameStatus(10);
    }

    public static void method164() {
        Renderable.handleSequences(GameInterface.fullscreenInterfaceId);
        if(GameInterface.fullscreenSiblingInterfaceId != -1)
            Renderable.handleSequences(GameInterface.fullscreenSiblingInterfaceId);
        MovedStatics.anInt199 = 0;
        ProducingGraphicsBuffer_Sub1.aProducingGraphicsBuffer_2213.prepareRasterizer();
        Player.viewportOffsets = Rasterizer3D.setLineOffsets(Player.viewportOffsets);
        Rasterizer.resetPixels();
        drawParentInterface(0, 0, 0, 765, 503, GameInterface.fullscreenInterfaceId);
        if(GameInterface.fullscreenSiblingInterfaceId != -1)
            drawParentInterface(0, 0, 0, 765, 503, GameInterface.fullscreenSiblingInterfaceId);
        if(!MovedStatics.menuOpen) {
            MovedStatics.processRightClick();
            MovedStatics.drawMenuTooltip(4);
        } else
            if(ScreenController.frameMode == ScreenMode.FIXED && MovedStatics.menuScreenArea == 0){
                MovedStatics.drawMenu(4,4); // might be 0,0
            }
        try {
            Graphics graphics = MouseHandler.gameCanvas.getGraphics();
            ProducingGraphicsBuffer_Sub1.aProducingGraphicsBuffer_2213.drawGraphics(0, 0, graphics);
        } catch(Exception exception) {
            MouseHandler.gameCanvas.repaint();
        }
    }

    public static void moveTowardsTarget() {
        // TODO (James) this moves the cutscene camera towards its target, we should move this into the CutsceneCamera class
        CutsceneCamera camera = Game.cutsceneCamera;

        int i = camera.getMoveTo().y;
        int i_3_ = camera.getMoveTo().x;
        int i_4_ = Scene.getFloorDrawHeight(Player.worldLevel, i_3_, i) - camera.getMoveTo().z;

        int newX = camera.getPosition().x;
        int newY = camera.getPosition().y;
        int newZ = camera.getPosition().z;

        if (i_3_ > newX) {
            newX += camera.getMovementSpeed().scale * (i_3_ - newX) / 1000 + camera.getMovementSpeed().base;
            if (newX > i_3_) {
                newX = i_3_;
            }
        }
        if (i_4_ > newZ) {
            newZ += camera.getMovementSpeed().scale * (i_4_ - newZ) / 1000 + camera.getMovementSpeed().base;
            if (i_4_ < newZ)
                newZ = i_4_;
        }
        if (newX > i_3_) {
            newX -= camera.getMovementSpeed().base + camera.getMovementSpeed().scale * (newX + -i_3_) / 1000;
            if (i_3_ > newX) {
                newX = i_3_;
            }
        }
        if (newY < i) {
            newY += camera.getMovementSpeed().base + camera.getMovementSpeed().scale * (-newY + i) / 1000;
            if (newY > i)
                newY = i;
        }
        if (newZ > i_4_) {
            newZ -= (newZ + -i_4_) * camera.getMovementSpeed().scale / 1000 + camera.getMovementSpeed().base;
            if (i_4_ > newZ)
                newZ = i_4_;
        }
        if (newY > i) {
            newY -= camera.getMovementSpeed().base + camera.getMovementSpeed().scale * (newY - i) / 1000;
            if (newY < i)
                newY = i;
        }

        camera.setPosition(new Point3d(newX, newY, newZ));

        i_3_ = camera.getLookAt().x;
        i = camera.getLookAt().y;
        i_4_ = Scene.getFloorDrawHeight(Player.worldLevel, i_3_, i) - camera.getLookAt().z;
        int i_5_ = -newZ + i_4_;
        int i_6_ = i - newY;
        int i_7_ = i_3_ - newX;
        int i_8_ = (int) Math.sqrt((double) (i_7_ * i_7_ + i_6_ * i_6_));
        int i_9_ = 0x7ff & (int) (Math.atan2((double) i_5_, (double) i_8_) * 325.949);
        if (i_9_ < 128)
            i_9_ = 128;
        int i_10_ = 0x7ff & (int) (-325.949 * Math.atan2((double) i_7_, (double) i_6_));
        if (i_9_ > 383)
            i_9_ = 383;
        int i_11_ = -camera.getRotation().yaw + i_10_;
        if (i_11_ > 1024)
            i_11_ -= 2048;
        if (i_11_ < -1024)
            i_11_ += 2048;

        int newYaw = camera.getRotation().yaw;
        int newPitch = camera.getRotation().pitch;

        if (i_11_ > 0) {
            newYaw += camera.getTurnSpeed().scale * i_11_ / 1000 + camera.getTurnSpeed().base;
            newYaw &= 0x7ff;
        }
        if (true) {
            if (i_11_ < 0) {
                newYaw -= camera.getTurnSpeed().base + camera.getTurnSpeed().scale * -i_11_ / 1000;
                newYaw &= 0x7ff;
            }
            if (i_9_ > newPitch) {
                newPitch += camera.getTurnSpeed().base + camera.getTurnSpeed().scale * (i_9_ - newPitch) / 1000;
                if (newPitch > i_9_)
                    newPitch = i_9_;
            }
            if (newPitch > i_9_) {
                newPitch -= camera.getTurnSpeed().scale * (newPitch + -i_9_) / 1000 + camera.getTurnSpeed().base;
                if (newPitch < i_9_)
                    newPitch = i_9_;
            }
            int i_12_ = i_10_ + -newYaw;
            if (i_12_ > 1024)
                i_12_ -= 2048;
            if (i_12_ < -1024)
                i_12_ += 2048;
            if (i_12_ < 0 && i_11_ > 0 || i_12_ > 0 && i_11_ < 0)
                newYaw = i_10_;
        }

        camera.rotate(newYaw, newPitch);
    }

    public static void updateGame() {
        if(MovedStatics.systemUpdateTime > 1)
            MovedStatics.systemUpdateTime--;
        if(SceneCluster.idleLogout > 0)
            SceneCluster.idleLogout--;
        if(aBoolean871) {
            aBoolean871 = false;
            Class59.dropClient();
        } else {
            for(int i = 0; i < 100; i++) {
                if(!IncomingPackets.parseIncomingPackets())
                    break;
            }
            if(gameStatusCode == 30 || gameStatusCode == 35) {
                if(aBoolean519 && gameStatusCode == 30) {
                    MouseHandler.currentMouseButtonPressed = 0;
                    MouseHandler.clickType = 0;
                    while(MovedStatics.method416()) {
                        /* empty */
                    }
                    for(int i = 0; i < Item.obfuscatedKeyStatus.length; i++)
                        Item.obfuscatedKeyStatus[i] = false;
                }
                ClientScriptRunner.createClientScriptCheckPacket(205, OutgoingPackets.buffer);
                synchronized(mouseCapturer.objectLock) {
                    if(MovedStatics.accountFlagged) {
                        if(MouseHandler.clickType != 0 || mouseCapturer.coord >= 40) {
                            int coordinateCount = 0;
                            OutgoingPackets.buffer.putPacket(210);
                            OutgoingPackets.buffer.putByte(0);
                            int originalOffset = OutgoingPackets.buffer.currentPosition;
                            for(int c = 0; c < mouseCapturer.coord; c++) {
                                if(-originalOffset + OutgoingPackets.buffer.currentPosition >= 240)
                                    break;
                                coordinateCount++;
                                int pixelOffset = mouseCapturer.coordsY[c];
                                if(pixelOffset >= 0) {
                                    if(pixelOffset > 502)
                                        pixelOffset = 502;
                                } else
                                    pixelOffset = 0;
                                int x = mouseCapturer.coordsX[c];
                                if(x < 0)
                                    x = 0;
                                else if(x > 764)
                                    x = 764;
                                int y = pixelOffset * 765 + x;
                                if(mouseCapturer.coordsY[c] == -1 && mouseCapturer.coordsX[c] == -1) {
                                    x = -1;
                                    y = -1;
                                    pixelOffset = 0x7ffff;
                                }
                                if(x == lastClickX && y == lastClickY) {
                                    if(duplicateClickCount < 2047)
                                        duplicateClickCount++;
                                } else {
                                    int differenceX = x - lastClickX;
                                    lastClickX = x;
                                    int differenceY = pixelOffset - lastClickY;
                                    lastClickY = pixelOffset;
                                    if(duplicateClickCount < 8 && differenceX >= -32 && differenceX <= 31 && differenceY >= -32 && differenceY <= 31) {
                                        differenceX += 32;
                                        differenceY += 32;
                                        OutgoingPackets.buffer.putShortBE(differenceY + (differenceX << 6) + (duplicateClickCount << 12));
                                        duplicateClickCount = 0;
                                    } else if(duplicateClickCount < 8) {
                                        OutgoingPackets.buffer.putMediumBE(y + 8388608 + (duplicateClickCount << 19));
                                        duplicateClickCount = 0;
                                    } else {
                                        OutgoingPackets.buffer.putIntBE((duplicateClickCount << 19) + -1073741824 + y);
                                        duplicateClickCount = 0;
                                    }
                                }
                            }
                            OutgoingPackets.buffer.finishVarByte(OutgoingPackets.buffer.currentPosition + -originalOffset);
                            if(coordinateCount < mouseCapturer.coord) {
                                mouseCapturer.coord -= coordinateCount;
                                for(int i_9_ = 0; mouseCapturer.coord > i_9_; i_9_++) {
                                    mouseCapturer.coordsX[i_9_] = mouseCapturer.coordsX[coordinateCount + i_9_];
                                    mouseCapturer.coordsY[i_9_] = mouseCapturer.coordsY[i_9_ + coordinateCount];
                                }
                            } else
                                mouseCapturer.coord = 0;
                        }
                    } else {
                        mouseCapturer.coord = 0;
                    }
                }
                if(MouseHandler.clickType != 0) {
                    long l = (MouseHandler.aLong2561 - aLong1203) / 50L;
                    int i = MouseHandler.clickX;
                    int i_10_ = MouseHandler.clickY;
                    aLong1203 = MouseHandler.aLong2561;
                    if(i >= 0) {
                        if(i > 764)
                            i = 764;
                    } else
                        i = 0;
                    if(i_10_ >= 0) {
                        if(i_10_ > 502)
                            i_10_ = 502;
                    } else
                        i_10_ = 0;
                    int i_11_ = 0;
                    if(MouseHandler.clickType == 2)
                        i_11_ = 1;
                    if(l > 4095)
                        l = 4095L;
                    int i_12_ = (int) l;
                    OutgoingPackets.buffer.putPacket(234);
                    int i_13_ = i_10_ * 765 + i;
                    OutgoingPackets.buffer.putIntLE((i_11_ << 19) + (i_12_ << 20) + i_13_);
                }
                if(InteractiveObject.anInt487 > 0)
                    InteractiveObject.anInt487--;
                if(Item.obfuscatedKeyStatus[96] || Item.obfuscatedKeyStatus[97] || Item.obfuscatedKeyStatus[98] || Item.obfuscatedKeyStatus[99])
                    MovedStatics.aBoolean565 = true;
                if(MovedStatics.aBoolean565 && InteractiveObject.anInt487 <= 0) {
                    InteractiveObject.anInt487 = 20;
                    MovedStatics.aBoolean565 = false;
                    OutgoingPackets.buffer.putPacket(58);
                    OutgoingPackets.buffer.putShortBE(Game.playerCamera.getYaw());
                    OutgoingPackets.buffer.putShortBE(Game.playerCamera.getPitch());
                }
                if(MovedStatics.aBoolean571 && !aBoolean1735) {
                    aBoolean1735 = true;
                    OutgoingPackets.buffer.putPacket(160);
                    OutgoingPackets.buffer.putByte(1);
                }
                if(!MovedStatics.aBoolean571 && aBoolean1735) {
                    aBoolean1735 = false;
                    OutgoingPackets.buffer.putPacket(160);
                    OutgoingPackets.buffer.putByte(0);
                }
                method910();
                if(gameStatusCode == 30 || gameStatusCode == 35) {
                    MovedStatics.method652();
                    SoundSystem.processSounds();
                    MusicSystem.processMusic();
                    IncomingPackets.cyclesSinceLastPacket++;
                    if (IncomingPackets.cyclesSinceLastPacket > 750) {
                        Class59.dropClient();
                    } else {
                        MovedStatics.animatePlayers(-1);
                        MovedStatics.animateNpcs();
                        MovedStatics.method313();
                        if(MovedStatics.crossType != 0) {
                            MovedStatics.crossIndex += 20;
                            if(MovedStatics.crossIndex >= 400)
                                MovedStatics.crossType = 0;
                        }
                        if(GameInterface.atInventoryInterfaceType != 0) {
                            RSRuntimeException.anInt1651++;
                            if(RSRuntimeException.anInt1651 >= 15) {
                                if(GameInterface.atInventoryInterfaceType == 2)
                                    GameInterface.redrawTabArea = true;
                                if(GameInterface.atInventoryInterfaceType == 3)
                                    ChatBox.redrawChatbox = true;
                                GameInterface.atInventoryInterfaceType = 0;
                            }
                        }
                        MovedStatics.anInt199++;
                        if(MovedStatics.activeInterfaceType != 0) {
                            Buffer.lastItemDragTime++;
                            if(MouseHandler.mouseX > Renderable.anInt2869 + 5 || Renderable.anInt2869 + -5 > MouseHandler.mouseX || MovedStatics.anInt2798 + 5 < MouseHandler.mouseY || MovedStatics.anInt2798 - 5 > MouseHandler.mouseY)
                                MovedStatics.lastItemDragged = true;
                            if(MouseHandler.currentMouseButtonPressed == 0) {
                                if(MovedStatics.activeInterfaceType == 3)
                                    ChatBox.redrawChatbox = true;
                                if(MovedStatics.activeInterfaceType == 2)
                                    GameInterface.redrawTabArea = true;
                                MovedStatics.activeInterfaceType = 0;
                                if(MovedStatics.lastItemDragged && Buffer.lastItemDragTime >= 5) {
                                    RSRuntimeException.lastActiveInvInterface = -1;
                                    MovedStatics.processRightClick();
                                    if(RSRuntimeException.lastActiveInvInterface == MovedStatics.modifiedWidgetId && mouseInvInterfaceIndex != GroundItemTile.selectedInventorySlot) {
                                        GameInterface childInterface = GameInterface.getInterface(MovedStatics.modifiedWidgetId);
                                        int moveItemInsertionMode = 0;
                                        if(MovedStatics.bankInsertMode == 1 && childInterface.contentType == 206)
                                            moveItemInsertionMode = 1;
                                        if(childInterface.items[mouseInvInterfaceIndex] <= 0)
                                            moveItemInsertionMode = 0;
                                        if(childInterface.itemDeletesDraged) {
                                            int i_16_ = mouseInvInterfaceIndex;
                                            int i_17_ = GroundItemTile.selectedInventorySlot;
                                            childInterface.items[i_16_] = childInterface.items[i_17_];
                                            childInterface.itemAmounts[i_16_] = childInterface.itemAmounts[i_17_];
                                            childInterface.items[i_17_] = -1;
                                            childInterface.itemAmounts[i_17_] = 0;
                                        } else if(moveItemInsertionMode == 1) {
                                            int slotStart = GroundItemTile.selectedInventorySlot;
                                            int slotEnd = mouseInvInterfaceIndex;
                                            while(slotEnd != slotStart) {
                                                if(slotStart <= slotEnd) {
                                                    if(slotStart < slotEnd) {
                                                        childInterface.swapItems(1 + slotStart, false, slotStart);
                                                        slotStart++;
                                                    }
                                                } else {
                                                    childInterface.swapItems(-1 + slotStart, false, slotStart);
                                                    slotStart--;
                                                }
                                            }
                                        } else
                                            childInterface.swapItems(mouseInvInterfaceIndex, false, GroundItemTile.selectedInventorySlot);

                                        OutgoingPackets.sendMessage(new DragWidgetItemOutboundMessage(
                                            moveItemInsertionMode,
                                            MovedStatics.modifiedWidgetId,
                                            GroundItemTile.selectedInventorySlot,
                                            mouseInvInterfaceIndex
                                        ));
                                    }
                                } else {
                                    if((ProducingGraphicsBuffer.oneMouseButton == 1 || MovedStatics.menuHasAddFriend(MovedStatics.menuActionRow - 1)) && MovedStatics.menuActionRow > 2)
                                        Class60.determineMenuSize();
                                    else if(MovedStatics.menuActionRow > 0)
                                        GameInterface.processMenuActions(MovedStatics.menuActionRow - 1);
                                }
                                RSRuntimeException.anInt1651 = 10;
                                MouseHandler.clickType = 0;
                            }
                        }

                        if(Scene.clickedTileX != -1) {
                            int i = Scene.clickedTileX;
                            int i_18_ = Scene.clickedTileY;
                            boolean bool = Pathfinding.doTileWalkTo(Player.localPlayer.pathY[0], Player.localPlayer.pathX[0], i, i_18_);
                            if(bool) {
                                GameInterface.crossY = MouseHandler.clickY;
                                MovedStatics.crossIndex = 0;
                                GameInterface.crossX = MouseHandler.clickX;
                                MovedStatics.crossType = 1;
                            }
                            Scene.clickedTileX = -1;
                        }

                        if(MouseHandler.clickType == 1 && Native.clickToContinueString != null) {
                            MouseHandler.clickType = 0;
                            ChatBox.redrawChatbox = true;
                            Native.clickToContinueString = null;
                        }

                        MouseHandler.processMenuClick();
                        if(GameInterface.fullscreenInterfaceId == -1) {
                            ScreenController.handleMinimapMouse();
                            ScreenController.handleTabClick();
                            ScreenController.handleChatButtonsClick();
                        }

                        if(MouseHandler.currentMouseButtonPressed == 1 || MouseHandler.clickType == 1)
                            Npc.anInt3294++;

                        int i = 34;
                        if(GameInterface.gameScreenInterfaceId != -1)
                            GameInterface.runClientScriptsForParentInterface(516, i, 338, GameInterface.gameScreenInterfaceId, 4, 4);

                        if(GameInterface.tabAreaInterfaceId == -1) {
                            if(Player.tabWidgetIds[Player.currentTabId] != -1)
                                GameInterface.runClientScriptsForParentInterface(743, i, 466, Player.tabWidgetIds[Player.currentTabId], 205, 553);
                        } else
                            GameInterface.runClientScriptsForParentInterface(743, i, 466, GameInterface.tabAreaInterfaceId, 205, 553);

                        if(GameInterface.chatboxInterfaceId != -1)
                            GameInterface.runClientScriptsForParentInterface(496, i, 453, GameInterface.chatboxInterfaceId, 357, 17);
                        else if(ChatBox.dialogueId != -1)
                            GameInterface.runClientScriptsForParentInterface(496, i, 453, ChatBox.dialogueId, 357, 17);

                        if(GameInterface.gameScreenInterfaceId != -1)
                            GameInterface.runClientScriptsForParentInterface(516, i ^ 0xffffffff, 338, GameInterface.gameScreenInterfaceId, 4, 4);

                        if(GameInterface.tabAreaInterfaceId != -1)
                            GameInterface.runClientScriptsForParentInterface(743, i ^ 0xffffffff, 466, GameInterface.tabAreaInterfaceId, 205, 553);

                        else if(Player.tabWidgetIds[Player.currentTabId] != -1)
                            GameInterface.runClientScriptsForParentInterface(743, i ^ 0xffffffff, 466, Player.tabWidgetIds[Player.currentTabId], 205, 553);

                        if(GameInterface.chatboxInterfaceId != -1)
                            GameInterface.runClientScriptsForParentInterface(496, i ^ 0xffffffff, 453, GameInterface.chatboxInterfaceId, 357, 17);
                        else if(ChatBox.dialogueId != -1)
                            GameInterface.runClientScriptsForParentInterface(496, i ^ 0xffffffff, 453, ChatBox.dialogueId, 357, 17);

                        // If hovering over a widget
                        if(MovedStatics.anInt1586 != -1 || MovedStatics.anInt614 != -1 || MovedStatics.anInt573 != -1) {
                            if(RSString.tooltipDelay > MovedStatics.durationHoveredOverWidget) {
                                MovedStatics.durationHoveredOverWidget++;
                                if(RSString.tooltipDelay == MovedStatics.durationHoveredOverWidget) {
                                    if(MovedStatics.anInt1586 != -1)
                                        ChatBox.redrawChatbox = true;
                                    if(MovedStatics.anInt614 != -1)
                                        GameInterface.redrawTabArea = true;
                                }
                            }
                        } else if(MovedStatics.durationHoveredOverWidget > 0)
                            MovedStatics.durationHoveredOverWidget--;
                        Item.calculateCameraPosition();
                        if(Player.cutsceneActive)
                            moveTowardsTarget();
                        for(int i_19_ = 0; i_19_ < 5; i_19_++)
                            SceneCamera.customCameraTimer[i_19_]++;
                        GameInterface.manageTextInputs();
                        int i_20_ = MouseHandler.resetFramesSinceMouseInput();
                        int i_21_ = KeyFocusListener.resetFramesSinceKeyboardInput();
                        if(i_20_ > 4500 && i_21_ > 4500) {
                            SceneCluster.idleLogout = 250;
                            MovedStatics.method650(4000);
                            OutgoingPackets.buffer.putPacket(216);
                        }

                        // antibot camera/minimap randomisation used to happen here

                        MovedStatics.anInt537++;
                        if(MovedStatics.anInt537 > 50) {
                            OutgoingPackets.buffer.putPacket(13);
                        }
                        do {
                            try {
                                if(MovedStatics.gameServerSocket == null || OutgoingPackets.buffer.currentPosition <= 0)
                                    break;
                                MovedStatics.gameServerSocket.sendDataFromBuffer(OutgoingPackets.buffer.currentPosition, 0, OutgoingPackets.buffer.buffer);
                                MovedStatics.anInt537 = 0;
                                OutgoingPackets.buffer.currentPosition = 0;
                            } catch(java.io.IOException ioexception) {
                                Class59.dropClient();
                                break;
                            }
                            break;
                        } while(false);
                    }
                }
            }
        }
    }

    public static void printHelp() {
        System.out.println("Usage: worldid, [live/office/local], [live/rc/wip], [lowmem/highmem], [free/members]");
        System.exit(1);
    }

    public static void handleLoginScreenActions() {
        try {
            if (loginStatus == 0) { // Initialize
                if (MovedStatics.gameServerSocket != null) {
                    MovedStatics.gameServerSocket.kill();
                    MovedStatics.gameServerSocket = null;
                }
                aBoolean871 = false;
                loginStatus = 1;
                anInt1756 = 0;
                MovedStatics.gameServerSignlinkNode = null;
            }
            if (loginStatus == 1) { // Create connection to server, and wait for it to become available
                if (MovedStatics.gameServerSignlinkNode == null) {
                    MovedStatics.gameServerSignlinkNode = signlink.createSocketNode(currentPort);
                }
                if (MovedStatics.gameServerSignlinkNode.status == 2) {
                    throw new IOException();
                }
                if (MovedStatics.gameServerSignlinkNode.status == 1) {
                    MovedStatics.gameServerSocket = new GameSocket((Socket) MovedStatics.gameServerSignlinkNode.value, signlink);
                    loginStatus = 2;
                    MovedStatics.gameServerSignlinkNode = null;
                }
            }
            if (loginStatus == 2) {
                long l = MovedStatics.aLong853 = RSString.nameToLong(Native.username.toString());
                OutgoingPackets.buffer.currentPosition = 0;
                OutgoingPackets.buffer.putByte(14);
                int i = (int) (0x1fL & l >> 16);
                OutgoingPackets.buffer.putByte(i);
                MovedStatics.gameServerSocket.sendDataFromBuffer(2, 0, OutgoingPackets.buffer.buffer);
                loginStatus = 3;
                IncomingPackets.incomingPacketBuffer.currentPosition = 0;
            }
            if (loginStatus == 3) {
                int i = MovedStatics.gameServerSocket.read();
                if (i != 0) {
                    displayMessageForResponseCode(i);
                    return;
                }
                IncomingPackets.incomingPacketBuffer.currentPosition = 0;
                loginStatus = 4;
            }
            if (loginStatus == 4) {

                if (IncomingPackets.incomingPacketBuffer.currentPosition < 8) {
                    int i = MovedStatics.gameServerSocket.inputStreamAvailable();
                    if (i > -IncomingPackets.incomingPacketBuffer.currentPosition + 8) {
                        i = -IncomingPackets.incomingPacketBuffer.currentPosition + 8;
                    }
                    if (i > 0) {
                        MovedStatics.gameServerSocket.readDataToBuffer(IncomingPackets.incomingPacketBuffer.currentPosition, i, IncomingPackets.incomingPacketBuffer.buffer);
                        IncomingPackets.incomingPacketBuffer.currentPosition += i;
                    }
                }
                if (IncomingPackets.incomingPacketBuffer.currentPosition == 8) {
                    IncomingPackets.incomingPacketBuffer.currentPosition = 0;
                    Renderable.aLong2858 = IncomingPackets.incomingPacketBuffer.getLongBE();
                    loginStatus = 5;
                }
            }
            if (loginStatus == 5) {
                int[] seeds = new int[4];
                seeds[0] = (int) (Math.random() * 9.9999999E7);
                seeds[1] = (int) (Math.random() * 9.9999999E7);
                seeds[2] = (int) (Renderable.aLong2858 >> 32);
                seeds[3] = (int) Renderable.aLong2858;
                OutgoingPackets.buffer.currentPosition = 0;
                OutgoingPackets.buffer.putByte(10);
                OutgoingPackets.buffer.putIntBE(seeds[0]);
                OutgoingPackets.buffer.putIntBE(seeds[1]);
                OutgoingPackets.buffer.putIntBE(seeds[2]);
                OutgoingPackets.buffer.putIntBE(seeds[3]);
                OutgoingPackets.buffer.putIntBE(signlink.uid);
                OutgoingPackets.buffer.putLongBE(RSString.nameToLong(Native.username.toString()));
                OutgoingPackets.buffer.method505(Native.password);
                if (Configuration.RSA_ENABLED) {
                    OutgoingPackets.buffer.applyRSA(Configuration.RSA_MODULUS, Configuration.RSA_PUBLIC_KEY);
                }


                // The actual login packet starts here

                MovedStatics.packetBuffer.currentPosition = 0;
                if (gameStatusCode == 40) {
                    // Reconnecting session
                    MovedStatics.packetBuffer.putByte(18);
                } else {
                    // New session
                    MovedStatics.packetBuffer.putByte(16);
                }
                MovedStatics.packetBuffer.putByte(57 + OutgoingPackets.buffer.currentPosition);
                MovedStatics.packetBuffer.putIntBE(435);
                MovedStatics.packetBuffer.putByte(VertexNormal.lowMemory ? 1 : 0);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.skeletonCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.skinDefinitionCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.gameDefinitionsCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.gameInterfaceCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.soundEffectCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.gameWorldMapCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.musicCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.modelCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.gameImageCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.gameTextureCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.huffmanCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.jingleCacheArchive.crc8);
                MovedStatics.packetBuffer.putIntBE(CacheArchive.clientScriptCacheArchive.crc8);
                MovedStatics.packetBuffer.putBytes(0, OutgoingPackets.buffer.currentPosition, OutgoingPackets.buffer.buffer);
                MovedStatics.gameServerSocket.sendDataFromBuffer(MovedStatics.packetBuffer.currentPosition, 0, MovedStatics.packetBuffer.buffer);
                OutgoingPackets.buffer.initOutCipher(seeds);

                // TODO (Jameskmonger) this allows the OutgoingPackets to access the ISAAC cipher. This is a hack and should be fixed.
                OutgoingPackets.init(OutgoingPackets.buffer.outCipher);
                
                for (int i = 0; i < 4; i++) {
                    seeds[i] += 50;
                }
                IncomingPackets.incomingPacketBuffer.initInCipher(seeds);
                loginStatus = 6;
            }



            if (loginStatus == 6 && MovedStatics.gameServerSocket.inputStreamAvailable() > 0) {
                int responseCode = MovedStatics.gameServerSocket.read();
                if (responseCode != 21 || gameStatusCode != 20) {
                    if (responseCode == 2) {
                        loginStatus = 9;
                    } else {
                        if (responseCode == 15 && gameStatusCode == 40) {
                            MovedStatics.method434();
                            return;
                        }
                        if (responseCode == 23 && MovedStatics.anInt2321 < 1) {
                            MovedStatics.anInt2321++;
                            loginStatus = 0;
                        } else {
                            displayMessageForResponseCode(responseCode);
                            return;
                        }
                    }
                } else {
                    loginStatus = 7;
                }
            }
            if (loginStatus == 7 && MovedStatics.gameServerSocket.inputStreamAvailable() > 0) {
                anInt784 = 180 + MovedStatics.gameServerSocket.read() * 60;
                loginStatus = 8;

            }
            if (loginStatus == 8) {
                anInt1756 = 0;
                Class60.setLoginScreenMessage(English.youHaveJustLeftAnotherWorld, English.yourProfileWillBeTransferredIn, (anInt784 / 60) + English.suffixSeconds);
                if (--anInt784 <= 0) {
                    loginStatus = 0;
                }
            } else {
                if (loginStatus == 9 && MovedStatics.gameServerSocket.inputStreamAvailable() >= 8) {
                    Configuration.USERNAME = Native.username.toString();
                    Configuration.PASSWORD = Native.password.toString();
                    InteractiveObject.playerRights = MovedStatics.gameServerSocket.read();
                    MovedStatics.accountFlagged = MovedStatics.gameServerSocket.read() == 1;
                    Player.localPlayerId = MovedStatics.gameServerSocket.read();
                    Player.localPlayerId <<= 8;
                    Player.localPlayerId += MovedStatics.gameServerSocket.read();
                    MovedStatics.anInt1049 = MovedStatics.gameServerSocket.read();
                    MovedStatics.gameServerSocket.readDataToBuffer(0, 1, IncomingPackets.incomingPacketBuffer.buffer);
                    IncomingPackets.incomingPacketBuffer.currentPosition = 0;
                    IncomingPackets.opcode = IncomingPackets.incomingPacketBuffer.getPacket();
                    MovedStatics.gameServerSocket.readDataToBuffer(0, 2, IncomingPackets.incomingPacketBuffer.buffer);
                    IncomingPackets.incomingPacketBuffer.currentPosition = 0;
                    IncomingPackets.incomingPacketSize = IncomingPackets.incomingPacketBuffer.getUnsignedShortBE();
                    loginStatus = 10;
                }
                if (loginStatus == 10) {
                    if (MovedStatics.gameServerSocket.inputStreamAvailable() >= IncomingPackets.incomingPacketSize) {
                        IncomingPackets.incomingPacketBuffer.currentPosition = 0;
                        MovedStatics.gameServerSocket.readDataToBuffer(0, IncomingPackets.incomingPacketSize, IncomingPackets.incomingPacketBuffer.buffer);
                        setConfigToDefaults();
                        MovedStatics.regionX = -1;
                        Landscape.constructMapRegion(false);
                        IncomingPackets.opcode = -1;
                    }
                } else {
                    anInt1756++;
                    if (anInt1756 > 2000) {
                        if (MovedStatics.anInt2321 < 1) {
                            MovedStatics.anInt2321++;
                            if (gameServerPort == currentPort) {
                                currentPort = CollisionMap.someOtherPort;
                            } else {
                                currentPort = gameServerPort;
                            }
                            loginStatus = 0;
                        } else {
                            displayMessageForResponseCode(-3);
                        }
                    }
                }
            }
        } catch (IOException ioexception) {
            if (MovedStatics.anInt2321 < 1) {
                if (currentPort == gameServerPort) {
                    currentPort = CollisionMap.someOtherPort;
                } else {
                    currentPort = gameServerPort;
                }
                MovedStatics.anInt2321++;
                loginStatus = 0;
            } else {
                displayMessageForResponseCode(-2);
            }
        }
    }

    private static void method947(int arg0) {
        synchronized(CollisionMap.anObject162) {
            if((Buffer.anInt1987 ^ 0xffffffff) != arg0) {
                Buffer.anInt1987 = 1;
                try {
                    CollisionMap.anObject162.wait();
                } catch(InterruptedException interruptedexception) {
                    /* empty */
                }
            }
        }
    }

    private static void renderNPCs(boolean arg0) {
        for(int i = 0; Player.npcCount > i; i++) {
            Npc npc = Player.npcs[Player.npcIds[i]];
            int i_15_ = 536870912 + (Player.npcIds[i] << 14);
            if(npc != null && npc.isInitialized() && arg0 == npc.actorDefinition.hasRenderPriority && npc.actorDefinition.isVisible()) {
                int i_16_ = npc.worldX >> 7;
                int i_17_ = npc.worldY >> 7;
                if(i_16_ >= 0 && i_16_ < 104 && i_17_ >= 0 && i_17_ < 104) {
                    if(npc.size == 1 && (npc.worldX & 0x7f) == 64 && (npc.worldY & 0x7f) == 64) {
                        if(MovedStatics.anIntArrayArray1435[i_16_][i_17_] == MovedStatics.anInt2628) {
                            continue;
                        }
                        MovedStatics.anIntArrayArray1435[i_16_][i_17_] = MovedStatics.anInt2628;
                    }
                    if(!npc.actorDefinition.isClickable) {
                        i_15_ += -2147483648;
                    }
                    currentScene.method134(Player.worldLevel, npc.worldX, npc.worldY, Scene.getFloorDrawHeight(Player.worldLevel, npc.worldX + (-1 + npc.size) * 64, npc.size * 64 + -64 + npc.worldY), -64 + npc.size * 64 + 60, npc, npc.anInt3118, i_15_, npc.aBoolean3105);
                }
            }
        }
    }

    private static void renderPlayers(int arg0, boolean arg1) {
        if(Player.localPlayer.worldX >> 7 == MovedStatics.destinationX && Player.localPlayer.worldY >> 7 == destinationY) {
            MovedStatics.destinationX = 0;

            DebugTools.walkpathX = null;
            DebugTools.walkpathY = null;
        }
        int i = Player.localPlayerCount;
        if(arg1)
            i = 1;
        int i_0_ = arg0;
        for(/**/; i > i_0_; i_0_++) {
            int i_1_;
            Player player;
            if(arg1) {
                i_1_ = 33538048;
                player = Player.localPlayer;
            } else {
                i_1_ = Player.trackedPlayerIndices[i_0_] << 14;
                player = Player.trackedPlayers[Player.trackedPlayerIndices[i_0_]];
            }
            if(player != null && player.isInitialized()) {
                player.aBoolean3287 = false;
                int tileX = player.worldX >> 7;
                int tileY = player.worldY >> 7;
                if((VertexNormal.lowMemory && Player.localPlayerCount > 50 || Player.localPlayerCount > 200) && !arg1 && player.anInt3077 == player.idleAnimation)
                    player.aBoolean3287 = true;
                if(tileX >= 0 && tileX < 104 && tileY >= 0 && tileY < 104) {
                    if(player.playerModel != null && player.anInt3283 <= MovedStatics.pulseCycle && MovedStatics.pulseCycle < player.anInt3274) {
                        player.aBoolean3287 = false;
                        player.anInt3276 = Scene.getFloorDrawHeight(Player.worldLevel, player.worldX, player.worldY);
                        currentScene.method112(Player.worldLevel, player.worldX, player.worldY, player.anInt3276, 60, player, player.anInt3118, i_1_, player.anInt3258, player.anInt3281, player.anInt3262, player.anInt3289);
                    } else {
                        if((0x7f & player.worldX) == 64 && (player.worldY & 0x7f) == 64) {
                            if(MovedStatics.anInt2628 == MovedStatics.anIntArrayArray1435[tileX][tileY])
                                continue;
                            MovedStatics.anIntArrayArray1435[tileX][tileY] = MovedStatics.anInt2628;
                        }
                        player.anInt3276 = Scene.getFloorDrawHeight(Player.worldLevel, player.worldX, player.worldY);
                        currentScene.method134(Player.worldLevel, player.worldX, player.worldY, player.anInt3276, 60, player, player.anInt3118, i_1_, player.aBoolean3105);
                    }
                }
            }
        }

    }

    public static void method943(int arg0, TypeFace arg2, int arg3, int arg4) {
        MovedStatics.chatModes.prepareRasterizer();
        MovedStatics.bottomChatBack.drawImage(0, 0);
        arg2.drawShadowedStringCenter(English.publicChat, 55, 28, 16777215, true);
        if(arg4 == 0)
            arg2.drawShadowedStringCenter(English.on, 55, 41, 65280, true);
        if(arg4 == 1)
            arg2.drawShadowedStringCenter(English.friends, 55, 41, 16776960, true);
        if(arg4 == 2)
            arg2.drawShadowedStringCenter(English.off, 55, 41, 16711680, true);
        if(arg4 == 3)
            arg2.drawShadowedStringCenter(English.hide, 55, 41, 65535, true);
        arg2.drawShadowedStringCenter(English.privateChat, 184, 28, 16777215, true);
        if(arg3 == 0)
            arg2.drawShadowedStringCenter(English.on, 184, 41, 65280, true);
        if(arg3 == 1)
            arg2.drawShadowedStringCenter(English.friends, 184, 41, 16776960, true);
        if(arg3 == 2)
            arg2.drawShadowedStringCenter(English.off, 184, 41, 16711680, true);
        arg2.drawShadowedStringCenter(English.trade, 324, 28, 16777215, true);
        if(arg0 == 0)
            arg2.drawShadowedStringCenter(English.on, 324, 41, 65280, true);
        if(arg0 == 1)
            arg2.drawShadowedStringCenter(English.friends, 324, 41, 16776960, true);
        if(arg0 == 2)
            arg2.drawShadowedStringCenter(English.off, 324, 41, 16711680, true);
        arg2.drawText(English.reportAbuse, 417, 17, 85, 25, 16777215, true, 1, 1, 0);
        try {
            Graphics graphics = MouseHandler.gameCanvas.getGraphics();
            if(ScreenController.frameMode == ScreenMode.FIXED) {
                MovedStatics.chatModes.drawGraphics(0, 453, graphics);
            }
        } catch(Exception exception) {
            MouseHandler.gameCanvas.repaint();
        }
    }

    public static void method910() {
        if(true) {
            if (VertexNormal.lowMemory && MovedStatics.onBuildTimePlane != Player.worldLevel)
                Actor.method789(Player.localPlayer.pathY[0], MovedStatics.regionY, MovedStatics.regionX, Player.localPlayer.pathX[0], Player.worldLevel);
            else if (Buffer.anInt1985 != Player.worldLevel) {
                Buffer.anInt1985 = Player.worldLevel;
                MovedStatics.method299(Player.worldLevel);
            }
        }
    }

    public static void logout() {
        if(MovedStatics.gameServerSocket != null) {
            MovedStatics.gameServerSocket.kill();
            MovedStatics.gameServerSocket = null;
        }
        RSCanvas.clearCaches();
        currentScene.initToNull();
        int i = 0;
        for(/**/; i < 4; i++)
            Landscape.currentCollisionMap[i].reset();
        System.gc();
        MusicSystem.method405(10);
        MusicSystem.songTimeout = 0;
        MusicSystem.currentSongId = -1;
        SoundSystem.clearObjectSounds();
        MovedStatics.processGameStatus(10);
    }

    public static void method249() {
        if(GameObject.frame != null) {
            synchronized(GameObject.frame) {
                GameObject.frame = null;
            }
        }
    }

    public void method35(int arg1) {
        if (currentPort != gameServerPort)
            currentPort = gameServerPort;
        else
            currentPort = CollisionMap.someOtherPort;
        updateServerSocket = null;
        ProducingGraphicsBuffer.updateServerSignlinkNode = null;
        MovedStatics.anInt292++;
        MovedStatics.connectionStage = 0;
        if (MovedStatics.anInt292 < 2 || arg1 != 7 && arg1 != 9) {
            if (MovedStatics.anInt292 < 2 || arg1 != 6) {
                if (MovedStatics.anInt292 >= 4) {
                    if (gameStatusCode <= 5) {
                        this.openErrorPage("js5connect");
                        anInt509 = 3000;
                    } else
                        anInt509 = 3000;
                }
            } else {
                this.openErrorPage("js5connect_outofdate");
                gameStatusCode = 1000;
            }
        } else if (gameStatusCode > 5)
            anInt509 = 3000;
        else {
            this.openErrorPage("js5connect_full");
            gameStatusCode = 1000;
        }
    }

    public void processGameLoop() {
        MovedStatics.pulseCycle++;
        handleUpdateServer();
        MovedStatics.handleRequests();
        MusicSystem.handleMusic();
        SoundSystem.handleSounds();
        GameInterface.method639();
        MouseHandler.method1015();

        if (gameStatusCode == 0) {
            Class40_Sub3.startup();
            Class60.method992();
        } else if (gameStatusCode == 5) {
            Class40_Sub3.startup();
            Class60.method992();
        } else if (gameStatusCode == 10) {
            Class60.updateLogin();
        } else if (gameStatusCode == 20) {
            Class60.updateLogin();
            handleLoginScreenActions();
        } else if (gameStatusCode == 25)
            Landscape.loadRegion();
        if (gameStatusCode == 30) {
            ScreenController.refreshFrameSize();
            updateGame();
        } else if (gameStatusCode == 35) {
            ScreenController.refreshFrameSize();
            updateGame();
        } else if (gameStatusCode == 40) {
            // Connection lost
            handleLoginScreenActions();
        }
    }

    public void handleUpdateServer() {
        if (gameStatusCode != 1000) {
            boolean bool = UpdateServer.processUpdateServerResponse();
            if (!bool)
                connectUpdateServer();
        }
    }

    /**
     * Sets the text that is shown in the middle of the screen depending on the current status code
     */
    public void updateStatusText() {
        if (MovedStatics.aBoolean1575) {
            KeyFocusListener.removeListeners(MouseHandler.gameCanvas);
            MouseHandler.removeListeners(MouseHandler.gameCanvas);
//            this.setCanvas();
            KeyFocusListener.addListeners(MouseHandler.gameCanvas);
            MouseHandler.addListeners(MouseHandler.gameCanvas);
        }
        if (gameStatusCode == 0)
            GameObject.drawLoadingText(MovedStatics.anInt1607, null, Native.currentLoadingText);
        else if (gameStatusCode == 5) {
            Class60.drawLoadingScreen(TypeFace.fontBold, TypeFace.fontSmall);
        } else if (gameStatusCode == 10) {
            Class60.drawLoadingScreen(TypeFace.fontBold, TypeFace.fontSmall);
        } else if (gameStatusCode == 20) {
            Class60.drawLoadingScreen(TypeFace.fontBold, TypeFace.fontSmall);
        } else if (gameStatusCode == 25) {
            if (ProducingGraphicsBuffer.anInt1634 == 1) {
                if (anInt874 > PacketBuffer.anInt2231)
                    PacketBuffer.anInt2231 = anInt874;
                int i = (-anInt874 + PacketBuffer.anInt2231) * 50 / PacketBuffer.anInt2231;
                MovedStatics.method940(English.loadingPleaseWait, true, Native.leftParenthesis + i + Native.percent_b);
            } else if (ProducingGraphicsBuffer.anInt1634 == 2) {
                if (anInt2591 > GameObject.anInt3048)
                    GameObject.anInt3048 = anInt2591;
                int i = 50 * (-anInt2591 + GameObject.anInt3048) / GameObject.anInt3048 + 50;
                MovedStatics.method940(English.loadingPleaseWait, true, Native.leftParenthesis + i + Native.percent_b);
            } else
                MovedStatics.method940(English.loadingPleaseWait, false, null);
        } else if (gameStatusCode == 30) {
            drawGameScreen();

        } else if (gameStatusCode == 35) {
            method164();
        } else if (gameStatusCode == 40)
            MovedStatics.method940(English.connectionLost, false, English.pleaseWaitAttemptingToReestablish);
        Npc.anInt3294 = 0;
    }

    public void connectUpdateServer() {
        if (UpdateServer.crcMismatches >= 4) {
            this.openErrorPage("js5crc");
            gameStatusCode = 1000;
        } else {
            if (UpdateServer.ioExceptions >= 4) {
                if (gameStatusCode > 5) {
                    UpdateServer.ioExceptions = 3;
                    anInt509 = 3000;
                } else {
                    this.openErrorPage("js5io");
                    gameStatusCode = 1000;
                    return;
                }
            }
            if (anInt509-- <= 0) {
                do {
                    try {
                        if (MovedStatics.connectionStage == 0) {
                            ProducingGraphicsBuffer.updateServerSignlinkNode = signlink.createSocketNode(currentPort);
                            MovedStatics.connectionStage++;
                        }
                        if (MovedStatics.connectionStage == 1) {
                            if (ProducingGraphicsBuffer.updateServerSignlinkNode.status == 2) {
                                method35(-1);
                                break;
                            }
                            if (ProducingGraphicsBuffer.updateServerSignlinkNode.status == 1)
                                MovedStatics.connectionStage++;
                        }
                        if (MovedStatics.connectionStage == 2) {
                            updateServerSocket = new GameSocket((Socket) ProducingGraphicsBuffer.updateServerSignlinkNode.value, signlink);
                            Buffer buffer = new Buffer(5);
                            buffer.putByte(15);
                            buffer.putIntBE(435); // Cache revision
                            updateServerSocket.sendDataFromBuffer(5, 0, buffer.buffer);
                            MovedStatics.connectionStage++;
                            MovedStatics.aLong1841 = System.currentTimeMillis();
                        }
                        if (MovedStatics.connectionStage == 3) {
                            if (gameStatusCode > 5 && updateServerSocket.inputStreamAvailable() <= 0) {
                                if (System.currentTimeMillis() + -MovedStatics.aLong1841 > 30000L) {
                                    method35(-2);
                                    break;
                                }
                            } else {
                                int i = updateServerSocket.read();
                                if (i != 0) {
                                    method35(i);
                                    break;
                                }
                                MovedStatics.connectionStage++;
                            }
                        }
                        if (MovedStatics.connectionStage != 4)
                            break;

                        UpdateServer.handleUpdateServerConnection(updateServerSocket, gameStatusCode > 20);

                        ProducingGraphicsBuffer.updateServerSignlinkNode = null;
                        MovedStatics.connectionStage = 0;
                        updateServerSocket = null;
                        MovedStatics.anInt292 = 0;
                    } catch (java.io.IOException ioexception) {
                        ioexception.printStackTrace();
                        method35(-3);
                        break;
                    }
                    break;
                } while (false);
            }
        }
    }

    public void setErrorHandler(GameErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void openErrorPage(String error) {
        if (this.errorHandler == null) {
            return;
        }

        this.errorHandler.handleGameError(error);
    }

    public void close() {
        if (mouseCapturer != null)
            mouseCapturer.aBoolean913 = false;
        mouseCapturer = null;
        if (MovedStatics.gameServerSocket != null) {
            MovedStatics.gameServerSocket.kill();
            MovedStatics.gameServerSocket = null;
        }
        MovedStatics.method744();
        method249();
        MusicSystem.syncedStop(false);
        SoundSystem.stop();
        UpdateServer.killUpdateServerSocket();
        method947(-1);
        do {
            try {
                if (dataChannel != null)
                    dataChannel.close();
                if (indexChannels != null) {
                    for (int i = 0; i < indexChannels.length; i++) {
                        if (indexChannels[i] != null)
                            indexChannels[i].close();
                    }
                }
                if (metaChannel == null)
                    break;
                metaChannel.close();
            } catch (java.io.IOException ioexception) {
                break;
            }
            break;
        } while (false);
    }


    public void startup() {
        // Define ports
        CollisionMap.someOtherPort = modewhere == 0 ? 443 : 50000 + Player.worldId;
        gameServerPort = modewhere != 0 ? Player.worldId + 40000 : Configuration.GAME_PORT;
        currentPort = gameServerPort;

        MovedStatics.method997();
        KeyFocusListener.addListeners(MouseHandler.gameCanvas);
        MouseHandler.addListeners(MouseHandler.gameCanvas);
        RSCanvas.anInt57 = Signlink.anInt737;
        try {
            if (signlink.cacheDataAccessFile != null) {
                dataChannel = new CacheFileChannel(signlink.cacheDataAccessFile, 5200);
                for (int i = 0; i < 13; i++)
                    indexChannels[i] = new CacheFileChannel(signlink.dataIndexAccessFiles[i], 6000);
                metaChannel = new CacheFileChannel(signlink.metaIndexAccessFile, 6000);
                metaIndex = new CacheIndex(255, dataChannel, metaChannel, 500000);
                signlink.dataIndexAccessFiles = null;
                signlink.metaIndexAccessFile = null;
                signlink.cacheDataAccessFile = null;
            }
        } catch (java.io.IOException ioexception) {
            metaIndex = null;
            dataChannel = null;
            metaChannel = null;
        }
        if (modewhere != 0)
            InteractiveObject.showFps = true;
        chatboxInterface = new GameInterface();
    }
}
