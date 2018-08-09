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

    handWritingView.onCandidatesAvailable(new HandWritingView.ResultHandler() {
                @Override
                public void receive(@NotNull List<? extends Candidate> candidates) {
                    //处理识别出的候选字列表
                }
            });

#### 选字
HandWritingView所返回的Candidate对象包含了选字所需要的所有信息和方法。它所对应的字存储在content字段里，
而当用户选取Candidate对应的字时，调用其select方法即可。用户所选择的字会通过HandWritingView的回调接口发送过来：

    handWritingView.onCandidateSelected(new CandidateButtonBar.CandidateHandler() {
                @Override
                public void selected(@NotNull String content) {
                    //处理用户选择的词
                }
            });

Candidate还有一个bind方法，用来将其与一个Button绑定。该方法实际上将Button的text设为它的content，
并设置Button的OnClickListener，使其在被点击时调用它的select方法。最简单的用法如下所示：

    for (Candidate candidate: candidates) {
        Button button = new Button(candidatesBar.getContext());
        candidate.bind(button);
        candidatesBar.addView(button);
    }