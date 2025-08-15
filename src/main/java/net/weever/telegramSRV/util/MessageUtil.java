//package net.weever.telegramSRV.util;
//
//import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
//import org.apache.commons.lang3.StringUtils;
//import org.bukkit.NamespacedKey;
//import org.bukkit.advancement.Advancement;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//public class MessageUtil {
//    private static final Map<NamespacedKey, String> ADVANCEMENT_TITLE_CACHE = new ConcurrentHashMap<>();
//    public static String getTitle(Advancement advancement) {
//        return ADVANCEMENT_TITLE_CACHE.computeIfAbsent(advancement.getKey(), v -> {
//            try {
//                Object handle = NMSUtil.getHandle(advancement);
//                assert handle != null;
//                Optional<Object> advancementDisplayOptional = getAdvancementDisplayObject(handle);
//                if (!advancementDisplayOptional.isPresent()) throw new RuntimeException("Advancement doesn't have display properties");
//
//                Object advancementDisplay = advancementDisplayOptional.get();
//                try {
//                    Field advancementMessageField = advancementDisplay.getClass().getDeclaredField("a");
//                    advancementMessageField.setAccessible(true);
//                    Object advancementMessage = advancementMessageField.get(advancementDisplay);
//                    Object advancementTitle = advancementMessage.getClass().getMethod("getString").invoke(advancementMessage);
//                    DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Successfully retrieved advancement title from getString");
//                    return (String) advancementTitle;
//                } catch (Exception e) {
//                    DiscordSRV.debug(Debug.MINECRAFT_TO_DISCORD, "Failed to get title of advancement using getString, trying JSON method");
//                }
//
//                Field titleComponentField = Arrays.stream(advancementDisplay.getClass().getDeclaredFields())
//                        .filter(field -> {
//                            String simpleFieldName = field.getType().getSimpleName();
//                            return simpleFieldName.equals("IChatBaseComponent") || simpleFieldName.equals("IChatMutableComponent") || simpleFieldName.equals("Component");
//                        })
//                        .findFirst().orElseThrow(() -> new RuntimeException("Failed to find advancement display properties field"));
//                titleComponentField.setAccessible(true);
//                Object titleChatBaseComponent = titleComponentField.get(advancementDisplay);
//                Method method_getText = null;
//                try {
//                    method_getText = titleChatBaseComponent.getClass().getMethod("getText");
//                } catch (Exception ignored) {}
//                if (method_getText == null) {
//                    try {
//                        method_getText = titleChatBaseComponent.getClass().getMethod("getString");
//                    } catch (Exception ignored) {}
//                }
//
//                if (method_getText != null) {
//                    String title = (String) method_getText.invoke(titleChatBaseComponent);
//                    if (StringUtils.isNotBlank(title)) return title;
//                }
//
//                Class<?> chatSerializerClass = Arrays.stream(titleChatBaseComponent.getClass().getDeclaredClasses())
//                        .filter(clazz -> clazz.getSimpleName().equals("ChatSerializer"))
//                        .findFirst().orElseThrow(() -> new RuntimeException("Couldn't get component ChatSerializer class"));
//                String componentJson = (String) chatSerializerClass.getMethod("a", titleChatBaseComponent.getClass()).invoke(null, titleChatBaseComponent);
//                return MessageUtil.toLegacy(GsonComponentSerializer.gson().deserialize(componentJson));
//            } catch (Exception e) {
//                String rawAdvancementName = advancement.getKey().getKey();
//                return Arrays.stream(rawAdvancementName.substring(rawAdvancementName.lastIndexOf("/") + 1).toLowerCase().split("_"))
//                        .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
//                        .collect(Collectors.joining(" "));
//            }
//        });
//    }
//
//    private static Method method_getAdvancementFromHolder = null;
//    private static Method method_getAdvancementDisplay = null;
//    private static Optional<Object> getAdvancementDisplayObject(Object handle) throws IllegalAccessException, InvocationTargetException {
//        if (handle.getClass().getSimpleName().equals("AdvancementHolder")) {
//            if (method_getAdvancementFromHolder == null) {
//                method_getAdvancementFromHolder = Arrays.stream(handle.getClass().getMethods())
//                        .filter(method -> method.getReturnType().getName().equals("net.minecraft.advancements.Advancement"))
//                        .filter(method -> method.getParameterCount() == 0)
//                        .findFirst().orElseThrow(() -> new RuntimeException("Failed to find Advancement from AdvancementHolder"));
//            }
//
//            if (method_getAdvancementFromHolder != null) {
//                Object holder = method_getAdvancementFromHolder.invoke(handle);
//
//                if (method_getAdvancementDisplay == null) {
//                    method_getAdvancementDisplay = Arrays.stream(holder.getClass().getMethods())
//                            .filter(method -> method.getReturnType().getSimpleName().equals("Optional"))
//                            .filter(method -> {
//                                String displayInfoReturnName = method.getGenericReturnType().getTypeName();
//                                return displayInfoReturnName.contains("AdvancementDisplay") || displayInfoReturnName.contains("DisplayInfo");
//                            })
//                            .findFirst().orElseThrow(() -> new RuntimeException("Failed to find AdvancementDisplay getter for advancement handle"));
//                }
//
//                @SuppressWarnings("unchecked")
//                Optional<Object> optionalAdvancementDisplay = (Optional<Object>) method_getAdvancementDisplay.invoke(holder);
//                return optionalAdvancementDisplay;
//            }
//        } else {
//            if (method_getAdvancementDisplay == null) {
//                method_getAdvancementDisplay = Arrays.stream(handle.getClass().getMethods())
//                        .filter(method -> {
//                            String simpleReturnName = method.getReturnType().getSimpleName();
//                            return simpleReturnName.equals("AdvancementDisplay") || simpleReturnName.equals("DisplayInfo");
//                        })
//                        .filter(method -> method.getParameterCount() == 0)
//                        .findFirst().orElseThrow(() -> new RuntimeException("Failed to find AdvancementDisplay getter for advancement handle"));
//            }
//
//            return Optional.ofNullable(method_getAdvancementDisplay.invoke(handle));
//        }
//        return Optional.empty();
//    }
//}
