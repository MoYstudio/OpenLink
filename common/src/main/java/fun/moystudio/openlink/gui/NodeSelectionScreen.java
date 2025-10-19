//注释的是收藏按钮相关代码，待收藏功能实现后可取消注释
package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpcimpl.OpenFrpFrpcImpl;
import fun.moystudio.openlink.json.JsonNode;
import fun.moystudio.openlink.json.JsonResponseWithData;
import fun.moystudio.openlink.json.JsonUserInfo;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class NodeSelectionScreen extends Screen {
    Screen lastscreen;
    NodeSelectionList selectionList;
    Button done;

    private static final ResourceLocation FAVORITE_ICON_TRUE = Utils.createResourceLocation("openlink", "textures/gui/favorite_true.png");
    private static final ResourceLocation FAVORITE_ICON_FALSE = Utils.createResourceLocation("openlink", "textures/gui/favorite_false.png");

    public NodeSelectionScreen(Screen lastscreen) {
        super(Utils.translatableText("gui.openlink.nodeselectionscreentitle"));
        this.lastscreen=lastscreen;
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }

    @Override
    protected void init() {
        if(selectionList==null){
            selectionList=new NodeSelectionList(this.minecraft);
        }
        selectionList.changePos(this.width, this.height, 32, this.height-65+4);
        this.addWidget(done=new Button(this.width / 2 - 100, this.height - 38, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            if(selectionList==null||selectionList.getSelected()==null||selectionList.getSelected().node.id==-1){
                OpenFrpFrpcImpl.nodeId=-1;
                this.minecraft.setScreen(lastscreen);
                return;
            }
            OpenFrpFrpcImpl.nodeId=selectionList.getSelected().node.id;
            this.minecraft.setScreen(lastscreen);
        }));
        this.addWidget(selectionList);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        this.renderBackground(poseStack);
        if(selectionList!=null){
            selectionList.render(poseStack,i,j,f);
            if(selectionList.userInfo!=null&&selectionList.userInfo.data!=null&&!selectionList.userInfo.data.realname) {
                drawString(poseStack,this.minecraft.font, Utils.translatableText("text.openlink.realnametounlock"),0,this.height-this.minecraft.font.lineHeight, 0xffffff);
            }
        }
        drawCenteredString(poseStack,this.font,this.title,this.width/2,16,0xffffff);
        done.render(poseStack,i,j,f);
    }

    class NodeSelectionList extends ObjectSelectionList<NodeSelectionList.Entry>{
        public NodeSelectionList(Minecraft minecraft) {
            super(minecraft, NodeSelectionScreen.this.width, NodeSelectionScreen.this.height, 32, NodeSelectionScreen.this.height-65+4, 40);
            JsonNode nothing=new JsonNode();
            nothing.name=CommonComponents.GUI_BACK.getString();
            nothing.id=-1;
            nothing.status=200;
            nothing.description=Utils.translatableText("text.openlink.node_autoselect").getString();
            Entry entry=new Entry(nothing);
            this.addEntry(entry);
            this.setSelected(entry);
            new Thread(()->{
                List<JsonNode> nodes;
                try {
                    nodes=OpenFrpFrpcImpl.getNodeList().data.list;
                    userInfo = OpenFrpFrpcImpl.getUserInfo();
                    for(JsonNode node:nodes){
                        if(SettingScreen.unavailableNodeHiding && userInfo!=null && userInfo.data!=null){
                            if(
                                node.fullyLoaded||
                                (node.needRealname&&!userInfo.data.realname)||
                                (!node.group.contains(userInfo.data.group))||
                                node.status!=200
                            ){
                                continue;
                            }
                        }
                        Entry entry1=new Entry(node);
                        this.addEntry(entry1);
                        if(node.id==OpenFrpFrpcImpl.nodeId){
                            this.setSelected(entry1);
                            this.centerScrollOn(entry1);
                        }
                    }
                } catch (Exception e) {
                    OpenLink.LOGGER.error("", e);
                    this.minecraft.setScreen(lastscreen);
                }
            },"Request thread").start();
            this.setRenderBackground(false);
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        public JsonResponseWithData<JsonUserInfo> userInfo = null;

        public void changePos(int width, int height, int y0, int y1){
            this.width=width;
            this.height=height;
            this.y0=y0;
            this.y1=y1;
        }

        @Override
        protected boolean isFocused(){
            return NodeSelectionScreen.this.getFocused() == this;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            super.render(poseStack, i, j, f);
        }

        public class Entry extends ObjectSelectionList.Entry<Entry>{
            JsonNode node;
            public Entry(JsonNode node){
                this.node=node;
//                favoriteButton = new ImageButton(0,0,20,20,0,0,0,FAVORITE_ICON_FALSE,20,20,button -> {
//                    changeFavoriteState();
//                });
            }

//            private void changeFavoriteState() {
//                //TODO: change favorite state
//                boolean favorite = true;//TODO: to be replaced with actual favorite state logic
//                favoriteButton = new ImageButton(0,0,20,20,0,0,0,favorite?FAVORITE_ICON_TRUE:FAVORITE_ICON_FALSE, button -> {
//                    changeFavoriteState();
//                });
//            }

            @Override
            public @NotNull Component getNarration() {
                return Utils.translatableText("narrator.select",this.node.name);
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
//                if (favoriteButton.mouseClicked(d,e,i)) return true;
                if (i == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }

//            ImageButton favoriteButton;

            private void select(){
                NodeSelectionList.this.setSelected(this);
            }

            @Override
            public void render(PoseStack poseStack, int i, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float f) {
                fill(poseStack,x,y,x+entryWidth,y+entryHeight,0x8f2b2b2b);
                String group=null;
                if(this.node.group!=null){
                    group = this.node.group.split(";")[0].toUpperCase();
                    if(group.equals("VIP")){
                        group="§e§l"+group;
                    }
                    if(group.equals("SVIP")){
                        group="§6§l"+group;
                    }
                }
                String description = this.node.description;
                boolean flag = false;
                while(x + 4 + NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(description+"...") > x + entryWidth - 4 - NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(this.node.bandwidth+"Mbps"+(this.node.bandwidthMagnification>1?" * "+this.node.bandwidthMagnification:""))) {
                    description=description.substring(0, description.length() - 1);
                    flag = true;
                }
                if(flag) {
                    description+="...";
                }
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, "#"+this.node.id+" "+this.node.name+(group!=null&&!group.equals("ADMIN")&&!group.equals("DEV")?" "+group:""), x + 4, y + 4, 0xffffffff);
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, description, x + 4, y + 4 + (entryHeight-4) / 2, 0xffffffff);
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.fullyLoaded||this.node.status!=200?Utils.translatableText("text.openlink.node_unavailable"):(this.node.needRealname?Utils.translatableText("text.openlink.node_needrealname"):Utils.translatableText("text.openlink.node_available")), x + entryWidth - 4 - NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(this.node.fullyLoaded||this.node.status!=200?Utils.translatableText("text.openlink.node_unavailable"):(this.node.needRealname?Utils.translatableText("text.openlink.node_needrealname"):Utils.translatableText("text.openlink.node_available"))), y + 4, 0xffffffff);
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.bandwidth+"Mbps"+(this.node.bandwidthMagnification>1?" * "+this.node.bandwidthMagnification:""), x + entryWidth - 4 - NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(this.node.bandwidth+"Mbps"+(this.node.bandwidthMagnification>1?" * "+this.node.bandwidthMagnification:"")), y + 4 + (entryHeight-4) / 2, 0xffffffff);
//                favoriteButton.x=x-25;
//                favoriteButton.y=(y+entryHeight)/2+10;
//                favoriteButton.render(poseStack, mouseX, mouseY, f);
                if(this.isMouseOver(mouseX, mouseY)) {
                    renderComponentTooltip(poseStack, Arrays.stream(this.node.description.replaceAll("(.{20})", "$1\n").split("\n")).map(s->(Component)Utils.literalText(s)).toList(), mouseX, mouseY);
                }
            }
        }
    }
}
