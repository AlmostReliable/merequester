---
navigation:
  title: ME Requester
  icon: requester
  position: 100
  item_ids:
    - merequester:requester
    - merequester:requester_terminal
---

# ME Requester

An add-on mod allowing you to keep items and fluids in your ME-System in stock.

### Getting Started

To get started, place a <ItemLink id="merequester:requester"/> and connect it to your network. Make sure to connect it to the same network
your <a href="ae2:autocrafting.md">Autocrafting</a> logic is located in. It should host your crafting CPUs and
<ItemLink id="ae2:pattern_provider"/>s.

After configuration, it will operate automatically and scan if the specified items or fluids fall below a certain threshold. When falling
below the threshold, it will request crafts until the desired amount is reached again. You can also configure a batch size to specify how
many crafts are requested at once.
