package pl.autoreroll;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;

public class RerollConfigScreen extends Screen {
	private static final int W = 280;
	private static final int H = 262;

	private static final int BG = 0xF0101018;
	private static final int HEADER = 0xFF1B1B2B;
	private static final int ACCENT = 0xFF7C5CFF;
	private static final int ROW = 0xFF181826;
	private static final int TEXT_DIM = 0xFFA8A8C0;
	private static final int GOLD = 0xFFFFD166;
	private static final int PURPLE = 0xFFB07CFF;

	private final Screen parent;
	private Slider minSlider, maxSlider;
	private TextFieldWidget limitField;

	private int px, py;

	public RerollConfigScreen(Screen parent) {
		super(Text.literal("AutoReroll"));
		this.parent = parent;
	}

	private static String fmt(double d) {
		return String.format("%.1f", d);
	}

	private static double round1(double d) {
		return Math.round(d * 10.0) / 10.0;
	}

	@Override
	protected void init() {
		RerollConfig c = AutoRerollClient.CONFIG;
		px = (width - W) / 2;
		py = Math.max(4, (height - H) / 2);

		minSlider = new Slider(px + 14, py + 56, W - 28, 20, "Zatrzymaj od", 0, 100, 0.1, c.min, "%", v -> {
			c.min = round1(v);
			if (c.min > c.max) {
				c.max = c.min;
				maxSlider.setReal(c.max);
			}
		});
		maxSlider = new Slider(px + 14, py + 80, W - 28, 20, "Zatrzymaj do", 0, 100, 0.1, c.max, "%", v -> {
			c.max = round1(v);
			if (c.max < c.min) {
				c.min = c.max;
				minSlider.setReal(c.min);
			}
		});
		addDrawableChild(minSlider);
		addDrawableChild(maxSlider);

		addDrawableChild(ButtonWidget.builder(Text.literal("Wybierz"), b -> beginLearn(AutoRerollClient.LearnMode.SWORD))
				.dimensions(px + W - 14 - 84, py + 120, 84, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Wybierz"), b -> beginLearn(AutoRerollClient.LearnMode.SCROLL))
				.dimensions(px + W - 14 - 84, py + 146, 84, 20).build());

		addDrawableChild(new Slider(px + 14, py + 184, 160, 20, "Opoznienie", 1, 20, 1, c.delayTicks, " tk",
				v -> c.delayTicks = (int) Math.round(v)));

		limitField = new TextFieldWidget(textRenderer, px + W - 14 - 84, py + 184, 84, 20, Text.empty());
		limitField.setMaxLength(6);
		limitField.setText(String.valueOf(c.maxRolls));
		addDrawableChild(limitField);

		addDrawableChild(ButtonWidget.builder(Text.literal("▶  Start"), b -> {
			save();
			AutoRerollClient.startFromGui(client);
		}).dimensions(px + 14, py + H - 34, 126, 22).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), b -> close())
				.dimensions(px + W - 14 - 126, py + H - 34, 126, 22).build());
	}

	private void beginLearn(AutoRerollClient.LearnMode mode) {
		save();
		AutoRerollClient.learn = mode;
		client.setScreen(new InventoryScreen(client.player));
	}

	private void save() {
		RerollConfig c = AutoRerollClient.CONFIG;
		try {
			c.maxRolls = Math.max(0, Integer.parseInt(limitField.getText().trim()));
		} catch (NumberFormatException ignored) {
		}
		c.save();
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);

		// panel
		ctx.fill(px - 1, py - 1, px + W + 1, py + H + 1, ACCENT);
		ctx.fill(px, py, px + W, py + H, BG);
		ctx.fill(px, py, px + W, py + 30, HEADER);
		ctx.fill(px, py + 30, px + W, py + 31, ACCENT);
		ctx.drawTextWithShadow(textRenderer, "AutoReroll", px + 14, py + 11, 0xFFFFFFFF);
		ctx.drawTextWithShadow(textRenderer, "klawisz R = start/stop", px + W - 14 - textRenderer.getWidth("klawisz R = start/stop"), py + 11, TEXT_DIM);

		// sekcje
		ctx.drawTextWithShadow(textRenderer, "ZAKRES ZATRZYMANIA", px + 14, py + 41, TEXT_DIM);
		ctx.drawTextWithShadow(textRenderer, "PRZEDMIOTY W EKWIPUNKU", px + 14, py + 107, TEXT_DIM);
		ctx.drawTextWithShadow(textRenderer, "USTAWIENIA", px + 14, py + 171, TEXT_DIM);
		ctx.drawTextWithShadow(textRenderer, "Limit rolli (0 = brak)", px + W - 14 - 84, py + 171, TEXT_DIM);

		row(ctx, py + 118, AutoRerollClient.CONFIG.swordIndex, "Miecz", GOLD);
		row(ctx, py + 144, AutoRerollClient.CONFIG.scrollIndex, "Zmianki", PURPLE);
	}

	private void row(DrawContext ctx, int y, int index, String label, int color) {
		ctx.fill(px + 14, y, px + W - 14 - 90, y + 24, ROW);
		ctx.fill(px + 14, y, px + 16, y + 24, color);

		ItemStack stack = ItemStack.EMPTY;
		if (client != null && client.player != null && index >= 0 && index <= 40) {
			stack = client.player.getInventory().getStack(index);
		}

		if (!stack.isEmpty()) {
			ctx.drawItem(stack, px + 22, y + 4);
			String name = stack.getName().getString();
			if (textRenderer.getWidth(name) > 100) name = textRenderer.trimToWidth(name, 96) + "..";
			ctx.drawTextWithShadow(textRenderer, label, px + 44, y + 3, color);
			ctx.drawTextWithShadow(textRenderer, name, px + 44, y + 13, 0xFFFFFFFF);
		} else {
			ctx.drawTextWithShadow(textRenderer, label, px + 24, y + 3, color);
			ctx.drawTextWithShadow(textRenderer, index < 0 ? "nie wybrano" : "brak przedmiotu", px + 24, y + 13, TEXT_DIM);
		}
	}

	@Override
	public void close() {
		save();
		client.setScreen(parent);
	}

	/** Suwak z etykieta i wartoscia. */
	private static class Slider extends SliderWidget {
		private final String label;
		private final double min, max, step;
		private final String suffix;
		private final DoubleConsumer onChange;

		Slider(int x, int y, int w, int h, String label, double min, double max, double step,
			   double value, String suffix, DoubleConsumer onChange) {
			super(x, y, w, h, Text.empty(), (value - min) / (max - min));
			this.label = label;
			this.min = min;
			this.max = max;
			this.step = step;
			this.suffix = suffix;
			this.onChange = onChange;
			updateMessage();
		}

		private double real() {
			double v = min + this.value * (max - min);
			return Math.max(min, Math.min(max, Math.round(v / step) * step));
		}

		void setReal(double v) {
			this.value = (v - min) / (max - min);
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			if (label == null) return;
			double r = real();
			String s = step >= 1 ? String.valueOf((long) r) : fmt(r);
			setMessage(Text.literal(label + ": " + s + suffix));
		}

		@Override
		protected void applyValue() {
			onChange.accept(real());
		}
	}
}
