### 简介
本項目封裝了灵云的手写识别sdk（包含多字识别和联想詞能力），并提供了简单的开箱即用组件：HandWritingView，提供
了一个手写板，会对笔迹进行识别，并将候选结果输出;CandidateButtonBar，用于接受候选结果，提供給用戶选择。

##### 使用
在项目根目录下的build.gradle里面加入maven中央仓库, 如下所示

    allprojects {
        repositories {
            // 其它仓库, 如google()等
            mavenCentral()
        }
    }

在要使用该库的模块的build.gradle中加入如下依赖即可:

    dependencies {
        // 其它依赖
        implementation 'com.github.charleslzq:hand-writing-view:1.0.0'
    }

#### 初始化
首先需要初始化灵云sdk，在Application的onCreate方法中加入如下代碼：

    HciHwrEngine.setup(this, "你的靈雲開發者key", "你的凌雲app key");

因为该过程包含联网验证，所以最好放在后台处理。 同时在应用退出时需要释放灵云引擎的资源，
在onTerminate方法中加入如下代码即可：

    HciHwrEngine.release();

HciHwrEngine还有两个方法，recognize和associate， 分别用于识别笔迹和获取联想词，如果使用
默认提供的组件的话就不需要直接调用这两个方法

#### 笔迹识别
在合适的位置使用com.github.charleslzq.hwr.HandWritingView即可。它本身是一个ImageView，会将
用户在它上面的触摸轨迹保留下来，并在每次用户手指离开时（写完一画时）将轨迹数据交给识别引擎处理，可以通过
如下方式获取识别结果：

    handWritingView.onResult(new HandWritingView.ResultHandler() {
                @Override
                public void receive(@NotNull List<? extends Candidate> candidates) {
                    //处理识别出的候选字列表
                }
            });

如果和CandidateButtonBar配合使用的话，也不需要直接使用该接口。
该组件提供了两个配置项：
1. enableAssociates, 是否开启联想功能，开启后它会将每次用户选择的候选词(及其前面的词，如果有的话)发送给灵云引擎，获取后续的候选词，默认为true
2. preTextLength， 最大前缀长度，每次送给灵云引擎进行联想的字符串的最大长度，默认为3

#### 选字
在合适的位置使用com.github.charleslzq.hwr.CandidateButtonBar，并在对应activity的onCreate方法中加入如下代码，以让其接受来自HandWritingView的候选
字并生成相应的选字按钮：

    candidateButtonBar.link(handWritingView);

与HandWritingView类似，当用户选择某个候选词后，它会通过回调将用户所选择的词发送过来：

    candidateButtonBar.onCandidateSelected(new CandidateButtonBar.CandidateHandler() {
                @Override
                public void selected(@NotNull String content) {
                    //处理用户选择的词
                }
            });
