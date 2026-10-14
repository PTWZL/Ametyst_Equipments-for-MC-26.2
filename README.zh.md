# 紫水晶装备 (Amethyst Equipment)

[English specification](README.en.md) · [项目主页 / Landing page](README.md)

Minecraft **26.2** + Fabric Loader **0.19.5** + Fabric API **0.159.0+26.2** 的 Fabric 模组。

新增一整套紫水晶装备与工具：紫水晶锭（由紫水晶碎片烧制，也有概率在废弃矿井的箱子里出现）、
剑、长矛、镐、斧、铲、锄，以及头盔 / 胸甲 / 护腿 / 靴子四件套。

**护甲、剑和长矛的数值会随玩家当前所处位置的光照强度变化。**

---

## 1. 数值规则

设 `x` = 玩家脚下位置的当前光照强度（0–15，取方块光与天空光的较大值，
因此夜晚和雷雨时会下降；下界只有方块光）。

### 护甲（每一件装备都按此计算）

| 项目 | 公式 |
| --- | --- |
| 护甲值 | 金护甲数值 `+ x × 0.8` |
| 击退抗性 | `+ x × 0.2` |

金护甲的原始护甲值（与原版完全一致）：

| 部位 | 基础护甲值 | 满光照（x=15）时 | 耐久 |
| --- | --- | --- | --- |
| 头盔 | 1 | 13 | 407 |
| 胸甲 | 3 | 15 | 592 |
| 护腿 | 5 | 17 | 555 |
| 靴子 | 2 | 14 | 481 |

> 每一件都独立获得 `x × 0.8` 的加成（即“金护甲数值 + x×0.8”按件计算），
> 因此穿满四件、站在满光照下时总护甲为 `11 + 4 × 15 × 0.8 = 59`，击退抗性 `+12`。

### 武器（剑与长矛）

| 项目 | 公式 |
| --- | --- |
| 攻击力 | `6 + x × 0.6` |

即光照 0 时 6 点，光照 15 时 **15** 点。

### 折光强化（等级 1–10）

设等级为 `L`，则**该物品的所有「每点光照」系数**乘以 `1 + 0.5 × L`：

| 等级 | 系数倍率 | 剑/长矛每点光照 | 护甲每点光照 | 击退抗性每点光照 |
| --- | --- | --- | --- | --- |
| 0（无） | ×1 | +0.6 | +0.8 | +0.2 |
| 1 | ×1.5 | +0.9 | +1.2 | +0.3 |
| 5 | ×3.5 | +2.1 | +2.8 | +0.7 |
| 10 | ×6 | +3.6 | +4.8 | +1.2 |

例：光照 15、折光强化 5 级的剑 = `6 + 15 × 0.6 × 3.5 = 37.5` 攻击力。

**获取与使用**

1. **合成 1 级书**：工作台 (1,1) 紫水晶块、(2,2) 钻石块、(3,3) 紫水晶块 → 折光强化书（1 级）。
2. **注入装备**：把折光强化书 + 任意一件紫水晶装备放进工作台无序合成，
   等级即写入该装备（取「书」与「装备已有等级」中较高者），书退还为一本普通书。
3. **升到更高级**：用 `/amethystequipment refraction <1-10>` 直接拿到对应等级的书，再重复第 2 步。
   同一件装备重复注入即可逐级提升。

> **关于「附魔」的重要说明（26.2 的平台限制）**
>
> 我最初把它实现为真正的数据驱动附魔，但在 26.2 上这**做不到**，并已实验确认：
>
> 1. 附魔注册表**只从原版自己的数据加载**。把 `data/<命名空间>/enchantment/refraction.json`
>    放进模组完全不会被读取——我故意把该文件写成非法 JSON，日志里连一条解析错误都没有；
>    启动时导出注册表内容，里面**恰好是 44 个原版附魔**，我们的一条也没有。
> 2. 代码里也注册不了：26.2 的 `BuiltInRegistries` **没有 `ENCHANTMENT` 字段**。
> 3. Fabric API 也没有对应钩子——`EnchantmentEvents` 只能观察/修改附魔过程，
>    不能新增附魔条目（我把 Fabric API 所有模块解包查过了）。
>
> 因此折光强化改用**自定义数据组件**实现。功能上就是你要的：
> 1–10 级、有名称与 tooltip、可合成、效果为「水晶光照系数 ×(1+0.5×等级)」。
> 唯一区别是它显示为独立的一行 tooltip，而非出现在原版附魔列表中。
> 若日后 Mojang 开放模组附魔注册，这部分可直接改回真附魔。

### 耐久

所有工具、武器、护甲均为**下界合金材质的 9/10**：

* 工具 / 剑 / 长矛：`2031 × 9/10 = 1827`
* 护甲：下界合金耐久系数 37，按部位 `× 9/10` 取整（见上表）

---

## 2. 物品与配方

配方形状与原版**完全相同**，只把材料换成紫水晶锭。

| 物品 | 配方 | 说明 |
| --- | --- | --- |
| 紫水晶锭 | 烧炼 / 高炉烧炼 1 个紫水晶碎片 | 经验 0.7，与原版矿石烧制一致 |
| 紫水晶剑 | `X` / `X` / `#` | `X`=紫水晶锭，`#`=木棍 |
| 紫水晶长矛 | `··X` / `·#·` / `#··` | 与原版长矛一致 |
| 紫水晶镐 | `XXX` / `·#·` / `·#·` | |
| 紫水晶斧 | `XX` / `X#` / `·#` | |
| 紫水晶铲 | `X` / `#` / `#` | |
| 紫水晶锄 | `XX` / `·#` / `·#` | |
| 紫水晶头盔 | `XXX` / `X·X` | |
| 紫水晶胸甲 | `X·X` / `XXX` / `XXX` | |
| 紫水晶护腿 | `XXX` / `X·X` / `X·X` | |
| 紫水晶靴子 | `X·X` / `X·X` | |
| 折光强化书（1 级） | `1··` / `·2·` / `··3` | `1`/`3`=紫水晶块，`2`=钻石块 |
| 折光强化注入 | 无序：折光强化书 + 任意紫水晶装备 | 把等级写入装备，书退还为普通书 |

* **废弃矿井**：在原有战利品表上**追加**一个独立奖池（不覆盖原版掉落），
  25% 概率掉落 1–2 个紫水晶锭。
* 紫水晶锭与折光强化书在“原料”创造标签页，装备在“战斗”标签页。
* 紫水晶工具/武器用紫水晶锭修复，护甲同样。

---

## 3. 安装

1. 为 Minecraft 26.2 安装 Fabric Loader。
2. 把 [Fabric API](https://modrinth.com/mod/fabric-api)（0.159.0+26.2 或更新）放进 `mods` 目录。
3. 把 `amethyst-equipment-1.0.0.jar`（`build/libs/` 产物或 Release 下载）放进同一个 `mods` 目录。
4. 启动游戏。

使用**版本隔离**的启动器（HMCL，以及 MultiMC/Prism 的隔离模式）中，目录是
`versions\26.2\mods` 而不是 `.minecraft\mods`。

### 构建

```bash
./gradlew build
```

需要 JDK 25，产物在 `build/libs/`。重新生成材质与数据文件：

```bash
python tools/generate_assets.py    # 需要 Python 3 + Pillow
```

生成结果已提交进仓库，因此这一步是可选的。它读取的取色源放在 `tools/reference/`
（见该目录的说明）。

---

## 4. 游戏内自检命令

需要权限等级 2（`/op`）。所有子命令都在 `/amethystequipment` 下：

| 命令 | 作用 |
| --- | --- |
| `/amethystequipment items` | 逐个报告 12 个物品的注册状态 |
| `/amethystequipment stats` | 打印耐久、护甲、每点光照的加成数值 |
| `/amethystequipment light` | 报告当前光照以及此刻的实际属性值 |
| `/amethystequipment light <0-15>` | 预览任意光照下的数值 |
| `/amethystequipment refraction <1-10>` | 获得对应等级的折光强化书 |
| `/amethystequipment grant` | 把全部 12 件物品给到自己背包 |

物品的 tooltip 也会实时显示当前光照强度、当前加成与满光照时的加成。

---

## 5. 工程结构

```
amethyst-equipment/
├─ build.gradle / gradle.properties / settings.gradle
├─ README.md                          中文文档（本文件）
├─ README.en.md                       英文文档
├─ tools/generate_assets.py          生成全部 JSON 与 PNG
└─ src/main/
   ├─ java/cn/amethyst/amethystequipment/
   │  ├─ AmethystEquipment.java      入口，常量与资源键
   │  ├─ AmethystItems.java          12 个物品 + 两种材料
   │  ├─ ModTags.java                修复用物品标签
   │  ├─ ModRecipes.java             配方序列化器/类型注册
   │  ├─ Refraction.java             折光强化数据组件
   │  ├─ RefractionApplyRecipe.java  「书 + 装备 → 注入等级」
   │  ├─ LightScaling.java           光照→属性 的核心逻辑（服务端 tick）
   │  ├─ LootTables.java             废弃矿井奖池
   │  ├─ CreativeTabs.java           创造模式标签页
   │  ├─ DebugCommands.java          /amethystequipment 命令
   │  └─ client/
   │     ├─ AmethystEquipmentClient.java
   │     └─ LightTooltip.java        tooltip 中的实时数值
   └─ resources/
      ├─ fabric.mod.json / pack.mcmeta
      ├─ assets/amethyst_equipment/  物品模型、装备贴图、语言文件
      └─ data/amethyst_equipment/    配方、标签
```

---

## 6. 实现要点（26.2 与旧版本的区别）

26.2 的物品不再有 `ArmorItem` / `TieredItem` / `SwordItem` 这些类，全部通过
`Item.Properties` 一次性配置：

```java
new Item.Properties().durability(1827).sword(AMETHYST_TOOL_MATERIAL, 5.0F, -2.4F)
new Item.Properties().durability(407).humanoidArmor(AMETHYST_ARMOR_MATERIAL, ArmorType.HELMET)
new Item.Properties().durability(1827).spear(AMETHYST_TOOL_MATERIAL, 0.85F, 0.82F, 0.65F, ...)
```

几个关键点：

* **攻击力**：原版会把玩家自身的 1.0 基础攻击力叠加到物品提供的数值上。
  为了显示“基础攻击力 6”，自定义 `ToolMaterial` 的攻击力加成设为 `0.0`，
  并在 `sword()` / `spear()` 里传 `5.0`，得到 `5.0 + 0.0 + 1.0 = 6.0`。
* **长矛的 9 个参数**（顺序很重要，一开始我就排错了导致右键动作异常）：
  `attackDuration, damageMultiplier, delay, dismountTime, dismountThreshold,
  knockbackTime, knockbackThreshold, damageTime, damageThreshold`。
  右键蓄力姿态时间轴由 `delay / dismountTime / knockbackTime / damageTime` 计算，
  必须满足 `dismountTime ≥ 1`、`knockbackTime ≥ dismountTime + 2`、
  `damageTime ≥ knockbackTime + 0.25`；`knockbackThreshold`(5.1) 与
  `damageThreshold`(4.6) 是所有原版长矛共用的常量。
  现在**逐字复制原版铜长矛**那一组数值（铜的 `attackDamageBonus` 同样是 `0.0`，
  走完全相同的代码分支）。启动时把紫水晶长矛与原版铜/钻石长矛的数据组件逐项打印对比，
  已确认 `kinetic_weapon`、`swing_animation`、`piercing_weapon`、`attack_range`、
  `use_effects` **完全一致**。
* **长矛模型必须用 `minecraft:item/generated`**（这才是真正导致持握/前刺动作错误的原因）：
  原版长矛的 GUI 模型 parent 是 `item/generated`，我一开始按"手持物品"的直觉写成了
  `item/handheld`。`item/handheld` 自带一套不同的 display 变换，会让手持长矛的姿势
  与蓄力前刺都偏掉——因为长矛的在手姿势是由物品定义里的 `display_context` 选择
  转发到 `_in_hand` 模型（parent `minecraft:item/spear_in_hand`）来控制的。
  改成 `item/generated` 后，紫水晶长矛的模型 JSON 与原版长矛逐字段一致。
* **光照加成**：护甲与武器的动态数值通过**服务端 tick 里的临时属性修饰符**实现
  （`Attributes.ARMOR`、`Attributes.KNOCKBACK_RESISTANCE`、`Attributes.ATTACK_DAMAGE`），
  只在「光照 / 所穿装备 / 折光等级」变化时重建，并带有缓存。服务端算完后由原版属性同步
  发给客户端，所以 tooltip 显示的数字与实际生效的数字一致。
* **物品图标**：26.2 的护甲物品用 `minecraft:item/generated` 模型，需要一张 16×16 的
  独立图标。**不能**复用穿着用的 `humanoid` 层贴图（那是 64×32 的未展开 UV 碎片，
  直接拿来当图标会显示成一堆错位像素——之前就是这个 bug）。
  现在四件护甲的图标**取自原版护甲贴图本身的像素**：头盔用头部正面 8×8、
  胸甲用躯干正面 8×12 加两侧手臂正面作护肩，再套用紫水晶色阶。因此造型与原版护甲一致。
  护腿与靴子因为原版护腿贴图只提供 4 像素高的髋部band、腿部件是零散小岛，
  这两件用同一套紫水晶色阶手工绘制轮廓，以保证与另外两件风格统一。
* **装备贴图**：`assets/amethyst_equipment/equipment/amethyst.json` 声明
  `humanoid` 与 `humanoid_leggings` 两层使用 `amethyst_equipment:amethyst`，
  对应
  `assets/amethyst_equipment/textures/entity/equipment/humanoid/amethyst.png` 与
  `.../humanoid_leggings/amethyst.png`（均为 64×32）。
* **材质来源**：工具与武器以原版钻石工具的像素形状重新上色为紫水晶色，
  护甲穿着层沿用原版下界合金护甲的像素布局重新上色（因此与玩家模型完全对齐），
  四件护甲图标与折光强化书图标为绘制/重上色所得。

## 7. 多语言

模组内置四份语言文件，未列出的语言一律回退到 `en_us`：

| 文件 | 语言 |
| --- | --- |
| `en_us.json` | 英文 —— 同时是所有其他语言的默认回退 |
| `zh_cn.json` | 简体中文 |
| `zh_tw.json` | 繁体中文（台湾） |
| `zh_hk.json` | 繁体中文（香港） |

即：客户端语言为任意中文变体时显示中文，其他语言显示英文。

## 8. 已知限制

* **折光强化不是原版附魔**，而是自定义数据组件。原因见第 1 节末尾：26.2 的附魔注册表
  只从原版数据加载，且没有留给模组的注册入口。功能等价，但显示为独立的 tooltip 行。
* 幼年生物穿戴时使用与成年相同的贴图（未提供 `humanoid_baby` 层）。
* 光照强度取“脚下位置方块光与天空光的较大值”，因此夜晚在户外会变弱，符合“随当前光照强度变化”的设定。
