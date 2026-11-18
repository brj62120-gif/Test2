package com.my.keyboard

// শব্দভান্ডার: প্রতিটি তালিকা ব্যবহারের হার (frequency) অনুযায়ী সাজানো — আগের শব্দ বেশি প্রচলিত।
// JVM এর string constant সীমা (৬৫৫৩৫ বাইট) এড়াতে শব্দগুলো ছোট ছোট টুকরায় জোড়া হয়েছে।
object WordData {
    val en: List<String> by lazy {
        val sb = StringBuilder()
        sb.append("i the be to of and a in that have it for not on with he as you do at this but his by from they we say her she or an will my one all would there their what so up out if about who get which go me when make can like time no just him know take people into year your good some could them see other than then now look only come its over think also back after use two how our work first well way even new want because any these give day most us is are was were been has had did does am being having doing said says went gone going got getting ")
        sb.append("made making took taken taking came coming saw seen seeing knew known knowing thought thinking told telling asked asking felt feeling found finding left leaving kept keeping let began begun begin seemed seem helped help showed show heard hearing played playing ran running moved moving lived living believed bring brought happen happened write wrote written writing provide sit sat stand stood lose lost pay paid meet met include included continue set learn learned change led lead understand watch follow stop create spoke speak read allow add spend grow opened open walk win offer remember love consider appear buy wait serve die send expect build stay fall cut reach kill remain hello hi hey thanks thank please sorry okay ok yes ")
        sb.append("yeah yep nope sure fine great nice cool awesome wonderful amazing excellent perfect lovely beautiful pretty better best bad worse worst happy sad angry tired hungry thirsty sleepy busy free ready late early soon later again always never sometimes often usually maybe perhaps probably definitely certainly actually really very quite too much many more less least few little lot lots enough almost already still yet where why whom whose whether while until unless since although though before during between among against without within through across around above below under behind beside beyond near far here everywhere anywhere somewhere nowhere today tomorrow yesterday tonight morning afternoon evening night midnight noon week weekend month hour minute second moment daily weekly monthly yearly ")
        sb.append("monday tuesday wednesday thursday friday saturday sunday january february march april may june july august september october november december friend friends family mother father mom dad brother sister son daughter wife husband baby child children kid kids parents uncle aunt cousin grandfather grandmother grandma grandpa neighbor boyfriend girlfriend partner colleague boss manager teacher student doctor nurse engineer driver farmer worker employee customer client member leader president minister officer police home house room kitchen bathroom bedroom door window floor wall roof garden street road city town village country world place area building office school college university hospital market shop store restaurant hotel bank airport station mosque temple church park beach river lake sea mountain forest island food water rice bread milk ")
        sb.append("tea coffee juice sugar salt egg eggs meat chicken fish beef vegetable vegetables fruit fruits apple banana mango orange potato tomato onion garlic cake cookie chocolate breakfast lunch dinner snack meal eat ate eating drink drank drinking cook cooking cooked phone mobile call calling called message messages text sent email mail internet wifi network online offline website app application software computer laptop tablet screen keyboard mouse camera photo picture video music song movie game games play player facebook whatsapp youtube google instagram twitter telegram messenger tiktok snapchat linkedin amazon netflix spotify money cash price cost payment bill salary income tax account card credit debit loan budget rich poor cheap expensive sell sale shopping order delivery working job career business company ")
        sb.append("meeting project plan task report document file folder deadline schedule appointment interview resume promotion class lesson exam test result marks grade study studying subject book books page pen pencil paper notebook homework assignment question answer solution problem example health medicine fever pain headache cold cough sick ill disease treatment exercise healthy sleep rest weather sun sunny rain rainy cloud cloudy wind storm snow hot warm temperature summer winter spring autumn body head face eye eyes ear ears nose mouth hand hands finger arm leg legs foot feet heart hair skin neck shoulder stomach color colour red blue green yellow black white pink purple brown grey gray golden silver number zero three four five six seven eight nine ten eleven twelve ")
        sb.append("thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty thirty forty fifty sixty seventy eighty ninety hundred thousand million billion third fourth fifth last next previous big small large huge tiny long short tall high low wide narrow thick thin heavy light fast slow quick quickly slowly easy easily hard difficult simple clear clean dirty empty full close closed old young modern ancient strong weak safe dangerous careful carefully important different same similar special common popular famous successful useful possible impossible necessary available particular certain true false real right wrong correct incorrect thing things something anything everything nothing someone anyone everyone nobody somebody anybody everybody whatever whenever wherever whoever however information education experience opportunity situation condition community government society culture history ")
        sb.append("language science technology development environment management service system program process product quality quantity reason purpose decision idea ideas opinion suggestion advice support request permission birthday wedding party celebration festival holiday vacation trip travel journey ticket flight train bus car bike cycle taxi ride driving drive traffic welcome congratulations wishes wish wishing blessing blessings prayer pray god peace joy luck hope dream dreams excuse pardon appreciate grateful thankful regards sincerely respectfully kindly truly faithfully assalamualaikum walaikum salam inshallah alhamdulillah mashallah subhanallah bismillah ramadan eid mubarak jummah allah islam muslim quran bangladesh dhaka chittagong sylhet rajshahi khulna barisal rangpur mymensingh comilla india pakistan nepal america england london canada australia japan china understood understanding explain explained explaining describe discuss discussion talk talking speaking ")
        sb.append("tell tells saying listen listening hear try trying tried start started starting finish finished finishing complete completed continued stopped waiting waited wanted wants need needed needs liked likes loved loves hate hated enjoy enjoyed prefer receive received sending forward reply replied respond response confirm confirmed cancel cancelled delay delayed postpone remind reminder bought buying bringing carry carrying carried hold holding held keep put putting push pull throw threw catch caught anytime sometime meanwhile otherwise therefore moreover furthermore nevertheless instead besides finally eventually suddenly immediately recently currently previously whereas either neither both each every another others such rather forget forgot forgotten forgive forgave mistake mistakes fix fixed repair broken damage missing share shared sharing post posted comment comments followers subscribe subscribed ")
        sb.append("profile status story stories update updates notification notifications download downloaded upload uploaded install installed delete deleted save saved edit edited copy paste select search searching find login logout password username settings setting handsome gorgeous cute smart clever intelligent brilliant talented kind gentle polite honest brave lazy proud humble generous friendly sweet funny silly crazy strange weird boring interesting exciting surprising shocking goodnight goodmorning goodbye bye cheers congratulation accept accepted agree agreed disagree believe doubt trust trusted guess suppose imagine realize realized notice noticed recognize anyway unfortunately fortunately hopefully honestly seriously obviously apparently basically generally especially exactly absolutely completely totally entirely mine yours ours theirs hers myself yourself himself herself itself ourselves themselves along alone amount apart aside away able access ")
        sb.append("according act action active activity address admit adult advantage afraid age ago ahead air alive anger animal annual apartment apply approach argue army arrive art article ask attack attention attempt attend audience author average avoid award aware balance ball band bar base basic battle beat beauty bed beer behavior belief benefit bird bit blood board boat bone border born bottom bowl box brain branch break bridge bright burn capital captain care case cause cell center century chair challenge chance character charge check choice choose citizen claim climate clock club coach coast collect comfort command compare compete complain concern conference confidence connect contact contain content control conversation corner count couple course court cover crowd cry current dance danger date dead deal ")
        sb.append("death debate decade deep defend degree demand depend design destroy detail develop device direct direction director discover dish distance divide dollar double drama draw dress drug dry earth east economy edge effect effort election electric element else emotion employ energy engine enter entire entry equal escape event evidence exact examine exist expert express extra fact factor fail failure fair fan fear feature fee field fight figure fill film final fire fit flag flat flow focus force foreign form formal frame freedom fresh front fuel fun function fund future gain gap gas gate general gift girl glass goal gold grand grass ground group guard guest guide gun habit half hall hang hat heat height hero hide hill hit hole honor horse ")
        sb.append("host human hurt ice image impact improve indeed industry inside interest iron issue item join judge jump jury justice key king knee knife knowledge lady land laugh law lawyer layer leaf lend length letter level library lie lift limit line link lip list local lock lonely machine magazine main major male manage manner map mark marriage master match material matter meaning measure media medical memory mental mention metal method middle military mind minor mirror miss mission mix model mood movement murder muscle museum nation native natural nature negative nervous noise normal north note nuclear object obtain obvious occasion ocean official oil operation oral organize original outside oven owner pack pair palace panel parent pass past patient pattern pause perform period ")
        sb.append("person personal phrase physical piece pilot pipe plain plane plant plastic plate pleasure plenty pocket poem poet point pole policy political pool population position positive pound power practice prepare present press pressure prevent pride prince print prison private prize produce professor profit promise proper protect prove public pure quarter queen quiet quote race radio rail raise range rare rate reaction reality recent record recover reduce refuse region regular relate relax release religion remove repeat replace represent require rescue research resource respect return review reward rhythm ring rise risk role roll romantic root rough round route row rule rush sand scale scene score season seat secret section security seed seek senior sense sentence separate series serious session settle shake shape sharp ")
        sb.append("sheet shell shift ship shoe shoot shot shout shut sign signal singer single sir site size skill sky slide slight smell smile smoke smooth soft soldier solid sort soul sound soup source south space spirit spot spread square staff stage star state steal steel step stick stock stone straight stranger stream strength stress strike structure style succeed sudden suffer suit suggest surface surprise survive swim switch symbol table tail talent target taste teach team tear telephone television tend term terrible theater theory threat throat tie tight till tip title tone tool tooth top total touch tour toward tower trade tradition training transfer transport trap treat tree trial trick trouble truck truth tube turn twice type ugly union unit upper upset ")
        sb.append("urban usual valley value variety various vehicle version victim view violence visit voice volume vote wage wake war warn wash waste wave wealth weapon wear weight wheel whole wild wine wing winner wire wise wonder wood word worry worth wrap yard youth zone achieve actor addition adjust admire advance affect afford agency agent agreement airline alarm album alcohol alert alphabet ambition analysis ancestor angle anniversary announce annoy anxious anymore apologize apology apparent appeal appearance applause approve arrange arrest arrival artist asleep aspect assist assume athlete atmosphere attach attitude attractive audio awake awful backpack backup bacon badge bag bake bakery balcony ban bandage bargain barely barrier basket bath battery bay beard bear beast beginner behave belly belong belt bench bend berry ")
        sb.append("bicycle bin biology birth biscuit bite bitter blame blank blanket bleed blend bless blind block blog blouse blow boil bold bomb bonus boot bore borrow bother bottle bounce brake brand breath breathe breed breeze brief broad broadcast brush bubble bucket buffet bug bulb bullet bunch burden burger bury butter button cabin cable cafe calculate calendar calm campaign camp campus canal candle candy cap capable capacity carbon cardboard carpet carrot cartoon castle casual catalog category cattle ceiling celebrate celebrity cement cereal ceremony certificate chain chalk champion channel chapter charity charm chart chase cheat cheek cheer cheese chef chemical cherry chest chew chief childhood chin chip chop chronic cinema circle circuit circumstance civil clap classic classroom clay click cliff climb clinic cloth ")
        sb.append("clothes clothing clue coal coat code coin collar collection colony column combine comedy comfortable commit committee communicate compact comparison compass complex component compose concentrate concept concert conclude concrete conduct confess conflict confuse congratulate connection conscious consequence conservative constant construct consult consume contest context contract contrast contribute convenient convince cooperate coral cord core corn corporate corridor cotton council counter countryside courage crack craft crash cream creative creature crew crime crisp critic crop cross crucial cruel cruise crush cuisine cup cupboard curious curly currency curtain curve custom dairy damp darling dash database dawn dealer dear debt decent decide declare decline decorate decrease deer defeat defence definition deliver demonstrate dentist deny department departure deposit depression depth desert deserve desk desperate dessert destination destiny ")
        sb.append("determine diagram dial diamond diary diet differ digital dignity dim dining dip diploma directly dirt disability disappear disappoint disaster discount discrimination disgust dislike dismiss display distant distinct district disturb dive diverse divorce dizzy dolphin domestic dominate donate donkey downtown dozen draft drag dragon drain drawer drawing drift drill drop drown drum dull dumb dump dust duty eager eagle earn earring earthquake echo edition editor educate effective efficient elbow elderly elect elegant elephant elevator eliminate embarrass emerge emergency emotional emperor emphasis employer enable encourage encounter endless enemy engage enormous ensure entertain enthusiasm entrance envelope envy episode equipment era error essay essential establish estate estimate ethnic evaluate everyday evil evolution exceed exchange excite executive exhibit exit expand expense explore export expose extend ")
        sb.append("extension extreme fabric facility fade fairly faith fake fame familiar fancy fantastic fare farewell farm fashion fasten fate fault favor favorite feather federal feed fellow female fence ferry fiber fiction fighter filter finance fingerprint firm fisherman fist fitness flash flavor flesh flexible flip float flood flour flower fluent fluid flute fly foam fog fold folk fond foolish football footstep forbid forecast forehead forever fork formula fortune forum fossil foundation fountain fragile frank freeze freight frequent friendship frighten frog frost frown frozen frustrate fry fulfill funeral fur furniture further gallery gamble garage garbage garment gather gear gene generate generation genius gentleman genuine geography gesture ghost giant giraffe glad glance glory glove glue goat goods gossip govern grab graceful gradual graduate grain ")
        sb.append("grammar grant grape graph grave gravity greet grief grill grin grocery guarantee guidance guilty guitar gym hairdresser halfway hammer handbag handle handwriting harbor hardly hardware harm harvest haste hatred haunt hazard headline heal heaven hedge heel helicopter helmet helpful herb heritage hesitate highway hike hint hip hire historic hobby holder honey hook horizon horn horrible horror hostile household hug humor hunger hunt hurry hut ideal identify identity idiot idle ignore illegal illness illustrate immediate immigrant impatient imply import impress impression imprison improvement incident increase incredible indicate individual indoor infant infection inflation influence inform ingredient inhabitant initial injury ink inner innocent insect insert insist inspect inspire instance instant institute instruct instrument insult insurance intend intense interrupt interval introduce invent invest investigate ")
        sb.append("invite involve irritate isolate jacket jail jam jar jaw jazz jealous jeans jelly jewel jewelry jog joke journal journalist jungle junior justify keen kettle kick kidney kindergarten kindness kingdom kiss kit knock knot label labor laboratory ladder lamp landlord landscape lane laser lately laundry lawn layout league leak lean leather lecture legal legend leisure lemon lens leopard liberty license lightning likely limb liquid literature lively loaf lobby location logic loose lorry lottery loud lounge loyal luggage lump lung luxury madam magic magnet maid mainly maintain majority makeup mall mammal manual manufacture marble margin marine marketing marry mass massive mate maximum mayor meadow measurement mechanic medal medium melon melt membership mercy mere merely merry mess metre microwave migrate mild mill mineral ")
        sb.append("minimum minus miracle mixture moderate modest moisture monitor monkey monster moral mosquito motel motion motivate motor motorcycle mount mud mug multiply mushroom musician mustard mutual mystery myth nail naked nasty navy nearby neat necklace needle neighbour nephew nest net neutral newspaper nickname niece noble nod nonsense noodle northern nowadays nut oak obey objective obligation observe occupy occur odd offend offence offense oldest olive operate opponent opposite option orbit orchestra ordinary organ organization origin ornament ought outcome outdoor outer outfit output outstanding overall overcome overseas overtime owe own oxygen pace package paddle painful paint painter pajamas palm pan pancake panic pants paperwork parade paragraph parcel parking parliament participate partly passage passenger passion passport pasta pastry patch path patience patrol pavement paw ")
        sb.append("peach peak pear pearl peasant pedal peer penalty pension pepper percent performance perfume permanent permit persist personality persuade pest pet petrol pharmacy phase philosophy photograph physics piano pick pickle picnic pie pig pill pillow pine pioneer pirate pit pitch pity pizza planet platform pleasant pledge plot plug plus poison polar polish pollution pond pony pop pork portion portrait pose possess postcard pot pour powder powerful praise precious precise predict pregnant premium preparation presence preserve prey primary prime principal principle priority privacy probable procedure proceed profession professional progress prohibit promote pronounce proof propose prospect protein protest proverb province psychology publish pump punch punish pupil purchase purse puzzle qualify quarrel quest queue quit quiz rabbit racket radar rage raid rainbow random rank ")
        sb.append("rapid raw ray razor reader realistic rear rebel receipt reception recipe recommend reconsider recording recruit recycle reflect reform refrigerator refund regard register regret reject relation relationship relative relevant reliable relief religious reluctant rely remark remote rent reporter republic reputation reserve resident resign resist resolve responsible restore restrict retire reveal revenue reverse revise revolution rib ribbon rid ridiculous rifle rigid ripe ritual rival roast rob robot rocket romance rope rose rotate rotten royal rubber rude rug ruin rural sack sacred sacrifice saddle safety sail salad salmon salute sample sandwich satellite satisfy sauce sausage savage scandal scarce scare scarf scatter scholar scholarship scissors scold scooter scope scratch scream screw script sculpture seal seasonal secondary secretary seldom selfish semester senate sensible sensitive sequence ")
        sb.append("servant settlement severe sew shadow shallow shame shampoo shark shave shelf shelter shepherd shine shiny shirt shiver shock shortage shorts shovel shower shrink shy sibling sidewalk sigh sightseeing signature significant silence silent silk similarity simply sin sincere sink skate skeleton sketch ski skirt skull slave slice slim slip slogan slope smash snake sneeze sniff soap soccer sociable social sock sofa soil solar solve somehow sorrow sour spare spark sparrow specialist species specific spectacular speech speed spell spicy spider spill spin spine spit splash spoil sponsor spoon sport spray sprinkle spy squeeze stable stadium stair stamp stare starve statement statue steady steam stem stereo sticky stiff stimulate sting stir stitch stool stove strain strap strategy straw strawberry stripe strive stroke stroll ")
        sb.append("struggle stubborn studio stuff stumble stupid subtle suburb subway sufficient suitcase sum summit sunrise sunset superb superior supermarket supply supreme surgeon surgery surround survey suspect suspend swallow swamp swan swear sweat sweater sweep swell swing sword sympathy tackle tag tailor tale tank tap tape tasty teapot teenage temper tempt tender tennis tense tent terminal terrific territory textbook texture theft thereby thesis thief thirst thorough thread thrill throne thumb thunder tidy timber timetable tin tire tissue toast toe toilet tolerate tomb tongue tonne toothbrush topic torch tortoise toss tough tourism tourist towel toy trace track tractor trader tragedy trail trailer trainer transform translate transparent trash treasure tremble trend tribe tribute triumph trophy tropical trousers trumpet trunk truthful tub tuition tune tunnel ")
        sb.append("turkey twin twist typical typist umbrella unable uncomfortable undergo underground undertake underwear unemployed unfair unfortunate uniform unique universal universe unknown unlike unlikely unpleasant unusual upcoming upgrade upright upstairs urge urgent usage utility vaccine vacuum vague valid valuable van vanish variable vary vase vast vegetarian venture verb verse vessel veteran victory viewer villain vine vinegar violent violin virtual virus visible vision visitor vital vitamin vivid vocabulary volcano volunteer vowel voyage waist waiter waitress wallet wander warehouse warning warrior wasp watermelon wax weakness weave weed weekday welfare western wet whale wheat whisper whistle wicked widow width wig willing wipe wisdom witch witness wolf womb wool workplace workshop worldwide worm worthwhile wound wrist writer yawn yell yogurt youngster zebra zip viber imo gmail ")
        sb.append("chrome android iphone samsung xiaomi oppo vivo realme huawei nokia windows linux microsoft uber pathao foodpanda bkash nagad daraz grameenphone robi banglalink airtel teletalk okey kk lol omg btw brb asap fyi idk ttyl thx pls plz sry ya yup nah gonna wanna gotta kinda sorta dunno anyways yaar bro bhai sis dude buddy guys gm gn zoom skype webinar seminar presentation slides submission midterm admission transaction recharge bundle data coverage sim respected greetings attached attachment enclosed forwarded regarding reference confirmation acknowledge invitation sehri iftar taraweeh tarawih juma fajr zuhr asr maghrib isha dua zakat sadaqah hajj umrah mecca madina fitr adha qurbani engagement valentine mothers fathers independence martyrs boishakh pohela puja durga diwali christmas whereby hereby therein herein accidentally ")
        sb.append("accordingly accurately additionally adequately admittedly aggressively amazingly annually anxiously approximately arguably artificially automatically awkwardly badly bitterly blindly boldly briefly brightly calmly carelessly casually cautiously cheerfully clearly closely coldly commonly confidently consistently constantly continually correctly courageously crazily curiously dangerously deeply deliberately delicately densely desperately differently doubtfully dramatically eagerly effectively efficiently elegantly emotionally endlessly energetically enormously enthusiastically equally essentially excellently exceptionally excitedly exclusively expensively extensively extremely famously fiercely firmly fluently foolishly formally frankly freely frequently fully furiously generously gently gladly gracefully gradually gratefully greatly happily hastily heavily helpfully highly hungrily importantly impossibly incorrectly increasingly independently inevitably innocently instantly intensely intentionally interestingly invariably ironically jokingly joyfully justly keenly largely lazily lightly literally locally loudly lovingly luckily madly mostly mysteriously namely nearly neatly nervously ")
        sb.append("nicely normally noticeably occasionally officially openly originally partially patiently perfectly permanently personally physically politely poorly possibly powerfully practically precisely presumably primarily privately promptly properly proudly purely quietly randomly rapidly rarely readily reasonably regularly relatively reluctantly remarkably repeatedly rightly roughly rudely sadly safely scarcely secretly severely sharply shortly shyly significantly silently similarly slightly smoothly softly solely specially specifically steadily strangely strictly strongly stupidly successfully sufficiently supposedly surely surprisingly suspiciously sweetly swiftly tenderly terribly thankfully thoroughly tightly traditionally tremendously typically ultimately unexpectedly uniformly unluckily utterly vaguely validly vastly violently visibly warmly weakly wearily wildly willingly wisely wonderfully worriedly wrongly accepting achieving adding adjusting admiring advising affecting agreeing allowing announcing answering appearing applying arranging arriving attending avoiding baking banking bathing beginning believing belonging ")
        sb.append("bending betting biking blaming blocking blowing boarding booking boiling borrowing bouncing breaking breathing burning camping canceling catching celebrating changing charging chatting checking cheering chewing choosing cleaning climbing closing collecting coloring combing commenting comparing competing complaining completing concerning connecting considering contacting continuing copying correcting counting covering crossing crying cutting dancing dealing deciding delivering depending describing designing destroying developing dialing discussing displaying disturbing diving dividing downloading dreaming dressing dropping drying earning editing emailing enjoying entering escaping exercising expecting exploring facing failing falling feeding fighting filling filming fishing fitting fixing flying folding following forgetting forgiving freezing frying gaining gathering giving glowing growing guessing guiding hanging happening hating healing helping hiding hitting hoping hugging hunting hurrying imagining improving including inviting joining jogging joking jumping ")
        sb.append("kicking killing kissing knocking landing lasting laughing launching leading learning lending letting lifting liking limiting loading locking looking losing loving managing marking marrying matching measuring mentioning mixing naming needing noticing offering opening ordering owning packing painting passing paying picking placing planning planting pleasing pointing polishing posting pouring practicing praying preparing pressing printing promising protecting proving pulling pushing questioning quitting racing raining raising reaching reading realizing receiving reducing refusing relaxing remembering renting repairing repeating replacing replying reporting requesting resting returning riding ringing rising rolling rubbing rushing sailing saving scoring scratching screaming seeking selecting selling serving settling shaking shaping shifting shining shouting showing shutting signing singing sitting skipping sleeping sliding smelling smiling smoking solving sorting sounding spelling spending spinning splitting spreading standing ")
        sb.append("staying stealing stepping sticking stopping storing succeeding suffering suggesting supporting supposing swimming switching teaching testing thanking throwing touching traveling treating turning typing using visiting voting waking walking wanting washing watching wearing weighing welcoming winning wondering worrying ")
        sb.append("imho tbh ty np nvm rn tmrw dm pm wi-fi signup reddit bing yahoo paypal pal folks fam bestie dating engaged bride groom honeymoon married roommate classmate teammate lemme gimme yo hmm huh oops wow whoa yay ugh aww haha hehe hihi congrats supper snacks meals menu ordered takeaway canteen baked fried boiled roasted grilled steamed curd ice-cream icecream cookies biscuits noodles fries ketchup spice spices chili chilli ginger cucumber cabbage cauliflower spinach pumpkin peas beans lentils pulses pudding sweets soda tray jug grapes guava papaya pineapple coconut lime plum pomegranate jackfruit lychee dates figs raisins almonds cashew peanuts walnut tshirt t-shirt pant saree sari kurta panjabi punjabi lungi shawl shoes sandals slippers socks glasses sunglasses bracelet earrings raincoat hoodie ")
        sb.append("lips teeth arms fingers capsule syrup injection flu stomachache toothache backache ache cure checkup prescription ambulance allergy diabetes cancer bacteria recovery yoga calories stairs couch mattress heater cooler fridge freezer cleaner broom toothpaste comb metro tram cab rickshaw auto launch visa reservation resort exams syllabus lecturer students tutor eraser ruler marker bookshelf leave resignation hiring cv applied candidate vacancy startup invoice withdraw loss investment shares desktop smartphone charger earphones headphones speaker microphone printer scanner router modem browser webpage uninstall reset restart shutdown reboot storage pendrive selfie screenshot emoji sticker gif meme reel follower subscriber live streaming trending viral hashtag inbox chat video-call missed ")
        sb.append("ringtone vibrate airplane bluetooth hotspot windy stormy humid monsoon drought cyclone mist moon stars dog cat cow sheep tiger lion duck hen rooster parrot pigeon crow butterfly bee ant fox camel crocodile turtle cricket badminton basketball volleyball hockey golf chess carrom ludo cycling boxing wrestling referee wicket run innings tournament semifinal loser theatre actress producer songs lyrics gaming gamer ramzan namaz masjid imam hindu christian buddhist easter caring scared worried stressed depressed mad fat excited surprised shocked confused disappointed embarrassed relieved annoyed offended frustrated bored cheerful lucky unlucky blessed naturally nonetheless regardless altogether anyhow somewhat timer agenda notes todo checklist pending done postponed rescheduled approved rejected ")
        sb.append("submitted suggestions feedback rating complaint questions answers problems solutions issues plans options choices decisions reasons results efforts success errors explanation description preference selected chose chosen decided promised apologise forgiven thanked blamed arrived reached depart departed returned entered exited visited invited joined gathered attended sold spent earned borrowed lent costs costing rented washed cleaned dried ironed ironing folded repaired broke dropped thrown pushed pulled lifted picked packed poured filled emptied woke woken slept dreamed bathe brushed dressed wore worn laughed smiled cried shouted whispered sing sang sung danced jumped climbed swam swum flew flown drove driven rode ridden tracking shipped delivered courier pincode widely several numerous ")
        sb.append("multiple triple west down centre side lower dark ")
        sb.append("abroad absent accident adventure angel ashamed barrel bean bell cave cigarette comic county crisis cultural detective dictionary difference dig downstairs drip elder except explode factory fairy favour favourite fierce flame flavour fool greedy harbour hell holy honour hopeless housework humour independent injure instruction labour lack lick lid lord nerve novel oppose organise pale penny pile pin pleased port practical pretend priest princess react realise refer roar rock rub rubbish scheme scientist secure shade shall shore sight sleeve splendid split standard steep stretch strict string strip technical teenager terror tyre unite virtue weigh whip ")
        sb.append("dms smh bff irl wfh eta rsvp apu vaiya dost ")
        sb.append("lmao rofl bruh hbd bday gnight wtf ikr fr ngl jk earphone headphone earbuds outlook teams pinterest biryani kacchi tehari khichuri paratha roti naan curry dal mutton sharbat halim kebab chowmein chips almond eggplant worldcup olympic fifa ipl bpl fasting tarabih tahajjud khutbah azan wudu covid corona mask sanitizer lockdown quarantine pandemic caption podcast blogger vlogger youtuber influencer wireless broadband tethering symptom diagnosis cholesterol asthma seatbelt flyover supervisor hired fired ")
        sb.append("applicant recruitment bangladeshi bengali bangla english hindi urdu arabic chinese japanese korean french german spanish italian russian turkish ")
        sb.toString().split(" ").filter { it.isNotEmpty() }.distinct()
    }

    val contractions: List<String> by lazy {
        val sb = StringBuilder()
        sb.append("i'm i'll i've i'd it's it'll don't doesn't didn't can't couldn't won't wouldn't shouldn't isn't aren't wasn't weren't haven't hasn't hadn't you're you'll you've you'd he's she's we're we'll we've they're they'll they've that's what's who's where's there's here's let's how's when's why's ")
        sb.append("ain't y'all could've should've would've must've might've tomorrow's today's yesterday's ")
        sb.append("it'd he'd she'd we'd they'd who'd who'll that'll there'll mustn't needn't mightn't shan't o'clock ma'am gonna's what'll what'd who're that'd there'd here're how'd where'd ")
        sb.toString().split(" ").filter { it.isNotEmpty() }.distinct()
    }

    val bn: List<String> by lazy {
        val sb = StringBuilder()
        sb.append("আমি তুমি আপনি সে আমরা তোমরা তারা এই ওই এটা ওটা সেটা এখানে ওখানে সেখানে কি কী কেন কোথায় কখন কেমন কত কে কাকে কার কোন কোনো কীভাবে কিভাবে না হ্যাঁ জি ঠিক আছে আছি আছো আছেন ছিল ছিলাম ছিলে ছিলেন ছিলো আমার আমাকে আমাদের আমাদেরকে তোমার তোমাকে তোমাদের তোমাদেরকে আপনার আপনাকে আপনাদের আপনাদেরকে তার তাকে তাদের তাদেরকে তাঁর তাঁকে তাঁদের এর এদের ওর ওকে ওদের নিজের নিজে নিজেকে করি করো করেন করে করা করছি করছো করছেন করছে করলাম করলে করলেন করল করলো করবো করব করবে করবেন করতে করেছি করেছো করেছেন করেছে করেছিলাম করেছিল করতাম করতেন করুন করিস হই হও হন হয় হয়ে হওয়া হচ্ছি হচ্ছো হচ্ছেন হচ্ছে হলাম হলে হলেন হল হলো হবো হব হবে হবেন হতে হয়েছি হয়েছো ")
        sb.append("হয়েছেন হয়েছে হয়েছিল হোক হতো হত যাই যাও যান যায় গিয়ে যাওয়া যাচ্ছি যাচ্ছো যাচ্ছেন যাচ্ছে গেলাম গেলে গেলেন গেল গেলো যাবো যাব যাবে যাবেন যেতে গিয়েছি গিয়েছো গিয়েছেন গিয়েছে গিয়েছিল আসি আসো আসেন আসে এসে আসা আসছি আসছো আসছেন আসছে এলাম এলে এলেন এল এলো আসবো আসব আসবে আসবেন আসতে এসেছি এসেছো এসেছেন এসেছে এসেছিল বলি বলো বলেন বলে বলা বলছি বলছো বলছেন বলছে বললাম বললে বললেন বলল বললো বলবো বলব বলবে বলবেন বলতে বলেছি বলেছো বলেছেন বলেছে বলেছিল বলুন দেখি দেখো দেখেন দেখে দেখা দেখছি দেখছো দেখছেন দেখছে দেখলাম দেখলে দেখলেন দেখল দেখলো দেখবো দেখব দেখবে দেখবেন দেখতে দেখেছি দেখেছো দেখেছেন দেখেছে দেখুন খাই খাও খান খায় খেয়ে খাওয়া খাচ্ছি খাচ্ছো খাচ্ছেন খাচ্ছে খেলাম খেলে খেলেন খেল খেলো ")
        sb.append("খাবো খাব খাবে খাবেন খেতে খেয়েছি খেয়েছো খেয়েছেন খেয়েছে খাবার দিই দাও দিন দেয় দিয়ে দেওয়া দিচ্ছি দিচ্ছো দিচ্ছেন দিচ্ছে দিলাম দিলে দিলেন দিল দিলো দেবো দেব দেবে দেবেন দিতে দিয়েছি দিয়েছো দিয়েছেন দিয়েছে নিই নাও নিন নেয় নিয়ে নেওয়া নিচ্ছি নিচ্ছো নিচ্ছেন নিচ্ছে নিলাম নিলে নিলেন নিল নিলো নেবো নেব নেবে নেবেন নিতে নিয়েছি নিয়েছো নিয়েছেন নিয়েছে পারি পারো পারেন পারে পারা পারছি পারছো পারছেন পারছে পারলাম পারলে পারল পারলো পারবো পারব পারবে পারবেন পারতে পেরেছি পেরেছো পেরেছেন পেরেছে জানি জানো জানেন জানে জানা জানছি জানলাম জানলে জানল জানবো জানব জানবে জানতে জেনেছি জেনেছো জেনেছে জানাই জানাবো জানান জানাও বুঝি বুঝো বোঝো বুঝেন বোঝেন বোঝে বুঝে বোঝা বুঝছি বুঝলাম বুঝলে বুঝল বুঝবো বুঝতে বুঝেছি বুঝেছো বুঝেছেন বুঝেছে শুনি শুনো ")
        sb.append("শোনো শুনেন শোনেন শোনে শুনে শোনা শুনছি শুনলাম শুনলে শুনল শুনবো শুনতে শুনেছি শুনেছো শুনেছেন শুনেছে শুনুন পড়ি পড়ো পড়েন পড়ে পড়া পড়ছি পড়ছো পড়ছেন পড়ছে পড়লাম পড়লে পড়ল পড়বো পড়তে পড়েছি পড়েছো পড়েছে পড়াশোনা লিখি লিখো লেখো লিখেন লেখেন লেখে লিখে লেখা লিখছি লিখছো লিখছেন লিখছে লিখলাম লিখলে লিখল লিখবো লিখতে লিখেছি লিখেছো লিখেছে লিখুন থাকি থাকো থাকেন থাকে থেকে থাকা থাকছি থাকছো থাকছেন থাকছে থাকলাম থাকলে থাকল থাকবো থাকব থাকবে থাকবেন থাকতে থেকেছি থেকেছো থেকেছে চাই চাও চান চায় চেয়ে চাওয়া চাচ্ছি চাচ্ছো চাচ্ছেন চাচ্ছে চাইলাম চাইলে চাইল চাইবো চাইতে চেয়েছি চেয়েছো চেয়েছেন চেয়েছে ধন্যবাদ দুঃখিত মাফ ক্ষমা অনেক অনেকদিন একটু একটা একটি একজন একসাথে একা এখন তখন যখন কখনো সবসময় মাঝে কখনও আবার আরও আরো শুধু শুধুমাত্র ")
        sb.append("প্রায় খুব বেশ বেশি কম বেশী আজ আজকে কাল আগামীকাল গতকাল পরশু আগামী গত এবার পরে আগে পরের আগের এখনই তাড়াতাড়ি দ্রুত ধীরে আস্তে সকাল দুপুর বিকাল বিকেল সন্ধ্যা রাত রাতে ভোর দিনে সপ্তাহ মাস বছর ঘণ্টা মিনিট সেকেন্ড সময় সময়ে সোমবার মঙ্গলবার বুধবার বৃহস্পতিবার শুক্রবার শনিবার রবিবার বৈশাখ জ্যৈষ্ঠ আষাঢ় শ্রাবণ ভাদ্র আশ্বিন কার্তিক অগ্রহায়ণ পৌষ মাঘ ফাল্গুন চৈত্র জানুয়ারি ফেব্রুয়ারি মার্চ এপ্রিল মে জুন জুলাই আগস্ট সেপ্টেম্বর অক্টোবর নভেম্বর ডিসেম্বর বসন্ত গ্রীষ্ম বর্ষা শরৎ হেমন্ত শীত মা বাবা আব্বু আম্মু আম্মা ভাই ভাইয়া বোন আপু আপা দাদা দাদি নানা নানি চাচা চাচি মামা মামি খালা ফুফু ফুফা বন্ধু বন্ধুরা সন্তান ছেলে মেয়ে বাচ্চা শিশু স্বামী স্ত্রী বউ পরিবার আত্মীয় প্রতিবেশী শ্বশুর শাশুড়ি মানুষ লোক পুরুষ নারী মহিলা ভদ্রলোক ")
        sb.append("ছাত্র ছাত্রী শিক্ষক শিক্ষিকা ডাক্তার নার্স ইঞ্জিনিয়ার কৃষক শ্রমিক ব্যবসায়ী চাকরি চাকুরি ড্রাইভার পুলিশ সৈনিক নেতা মন্ত্রী প্রধানমন্ত্রী রাষ্ট্রপতি সরকার জনগণ দেশ জাতি বাড়ি বাসা ঘর রুম রান্নাঘর বাথরুম দরজা জানালা ছাদ উঠান বাগান রাস্তা গলি শহর গ্রাম গ্রামে জেলা বিভাগ দোকান বাজার হাট মার্কেট স্কুল কলেজ বিশ্ববিদ্যালয় মাদ্রাসা মসজিদ মন্দির গির্জা হাসপাতাল অফিস কারখানা ব্যাংক স্টেশন বিমানবন্দর হোটেল রেস্টুরেন্ট পার্ক নদী সাগর সমুদ্র পাহাড় বন জঙ্গল মাঠ আকাশ মাটি পানি জল আগুন বাতাস হাওয়া সূর্য চাঁদ মেঘ বৃষ্টি ঝড় রোদ কুয়াশা গরম ঠান্ডা ভাত রুটি পরোটা ডাল তরকারি সবজি মাছ মাংস গরু খাসি মুরগি ডিম দুধ চা কফি চিনি লবণ তেল মসলা ফল আম কলা আপেল কমলা পেঁপে তরমুজ লিচু কাঁঠাল আনারস আঙুর পেয়ারা আলু পেঁয়াজ রসুন মরিচ টমেটো ")
        sb.append("বেগুন কুমড়া লাউ শসা গাজর মিষ্টি পায়েস পিঠা বিরিয়ানি খিচুড়ি নাস্তা দুপুরের রাতের রান্না টাকা পয়সা দাম মূল্য খরচ বেতন আয় ব্যয় ঋণ হিসাব কার্ড কেনা বেচা বিক্রি কিনি কিনব কিনেছি দোকানদার ক্রেতা বিক্রেতা সস্তা দামি ফোন মোবাইল মেসেজ কল কথা কথাবার্তা খবর সংবাদ ইন্টারনেট ওয়াইফাই নেটওয়ার্ক অ্যাপ অ্যাপ্লিকেশন কম্পিউটার ল্যাপটপ ক্যামেরা ছবি ভিডিও গান সিনেমা নাটক খেলা ফেসবুক হোয়াটসঅ্যাপ ইউটিউব গুগল ইমেইল পাসওয়ার্ড অ্যাকাউন্ট সেটিংস ডাউনলোড আপলোড শেয়ার লাইক কমেন্ট পোস্ট ফলো কাজ কাজে কাজের ব্যবসা মিটিং প্রজেক্ট পরিকল্পনা পরিকল্পনায় রিপোর্ট ফাইল সময়সূচি সাক্ষাৎকার ছুটি ছুটির দায়িত্ব সফল সফলতা ব্যর্থ ব্যর্থতা চেষ্টা চেষ্টায় পরিশ্রম শ্রেণি ক্লাস পাঠ পরীক্ষা ফলাফল নম্বর বই খাতা কলম পেন্সিল কাগজ শিক্ষা জ্ঞান বিদ্যা বিষয় বিজ্ঞান গণিত ইংরেজি বাংলা ইতিহাস ভূগোল প্রশ্ন উত্তর সমস্যা সমাধান ")
        sb.append("উদাহরণ স্বাস্থ্য রোগ অসুখ জ্বর ব্যথা মাথা মাথাব্যথা সর্দি কাশি ওষুধ চিকিৎসা ঘুম বিশ্রাম ব্যায়াম সুস্থ অসুস্থ হাত পা চোখ কান নাক মুখ দাঁত চুল পেট বুক পিঠ গলা কাঁধ আঙুল হৃদয় মন শরীর রক্ত ভালো ভালোবাসা ভালোবাসি ভালোবাসো ভালোবাসেন খারাপ সুন্দর অসুন্দর বড় ছোট লম্বা খাটো মোটা পাতলা নতুন পুরনো পুরোনো সহজ কঠিন দামী ভারী হালকা শক্ত নরম টক ঝাল তেতো লাল নীল সবুজ হলুদ কালো সাদা গোলাপি বেগুনি বাদামি ধূসর সুখ সুখী দুঃখ দুঃখী আনন্দ খুশি রাগ ভয় চিন্তা চিন্তিত লজ্জা হাসি কান্না মজা মজার দারুণ চমৎকার অসাধারণ দুর্দান্ত অবাক বিরক্ত ক্লান্ত ব্যস্ত অলস পরিষ্কার নোংরা নিরাপদ বিপদ বিপজ্জনক শুভ শুভেচ্ছা শুভকামনা শুভরাত্রি জন্মদিন বিয়ে বিবাহ অনুষ্ঠান উৎসব পার্টি উপহার ভ্রমণ ঘুরতে যাত্রা টিকিট ট্রেন বাস গাড়ি রিকশা ")
        sb.append("বাইক সাইকেল নৌকা লঞ্চ বিমান ফ্লাইট স্বাগতম অভিনন্দন অভিনন্দনও দোয়া দোয়ায় আল্লাহ খোদা ঈশ্বর ভগবান নামাজ রোজা ঈদ মোবারক রমজান কোরআন ইসলাম মুসলিম সালাম আসসালামু আলাইকুম ওয়ালাইকুম ইনশাআল্লাহ আলহামদুলিল্লাহ মাশাআল্লাহ সুবহানাল্লাহ বিসমিল্লাহ আমিন জুমা দুআ বাংলাদেশ ঢাকা চট্টগ্রাম সিলেট রাজশাহী খুলনা বরিশাল রংপুর ময়মনসিংহ কুমিল্লা কক্সবাজার গাজীপুর নারায়ণগঞ্জ বগুড়া ভারত পাকিস্তান নেপাল আমেরিকা ইংল্যান্ড কানাডা জাপান চীন সৌদি এবং অথবা বা কিন্তু তবে যদি তাহলে তাই তো কারণ কারণে জন্য সঙ্গে সাথে পর্যন্ত মধ্যে ভিতরে ভেতরে বাইরে উপরে নিচে সামনে পেছনে পাশে কাছে দূরে সবাই সব সবকিছু কিছু কিছুই কেউ কেউই কোনোকিছু কোথাও কোথাওই যেখানে যেমন তেমন যত তত যতটা ততটা অন্য অন্যান্য আর অবশ্য অবশ্যই নিশ্চয়ই হয়তো হয়ত সম্ভব সম্ভবত সত্যি সত্য মিথ্যা এক দুই তিন চার পাঁচ ছয় ")
        sb.append("সাত আট নয় দশ এগারো বারো তেরো চৌদ্দ পনেরো ষোলো সতেরো আঠারো উনিশ বিশ ত্রিশ চল্লিশ পঞ্চাশ ষাট সত্তর আশি নব্বই শত শতক হাজার লক্ষ লাখ কোটি প্রথম দ্বিতীয় তৃতীয় চতুর্থ পঞ্চম শেষ পরবর্তী আধা অর্ধেক দেড় আড়াই দুটি তিনটি চারটি পাঁচটি দুইটি তিনটা চারটা পাঁচটা দুজন তিনজন চারজন পাঁচজন প্রয়োজন দরকার জরুরি গুরুত্বপূর্ণ সম্ভাবনা সুযোগ সুবিধা অসুবিধা ঝামেলা সমস্যায় পরিস্থিতি অবস্থা অবস্থান ব্যবস্থা পদ্ধতি উপায় ফলে কারণেই উদ্দেশ্য লক্ষ্য স্বপ্ন আশা ইচ্ছা সিদ্ধান্ত মতামত পরামর্শ সাহায্য সহযোগিতা অনুরোধ অনুমতি অনুগ্রহ দয়া প্লিজ মনে ভুলে ভুল রাখো রাখি নেই বিশ্বাস সন্দেহ নিরাশ অপেক্ষা অপেক্ষায় প্রতীক্ষা দেরি দেরিতে তাড়া তাড়াহুড়ো শুরু শুরুতে শেষে থামো থামা থামি চলো চলি চলুন চলে চলা চলছি চলছে চলবে চলতে ফিরি ফিরে ফেরা ফিরব ফিরবো খুলি ")
        sb.append("খোলা খুলুন বন্ধ বন্ধুত্ব ধরি ধরো ধরা রাখা রাখুন ছাড়ি ছাড়ো ছাড়া ছেড়ে ডাকি ডাকো ডাকা ডাকছি কাছাকাছি সাথেই জেনে ভেবে ভাবি ভাবো ভাবছি ভাবনা ধারণা কল্পনা মনোভাব অনুভূতি অনুভব নাম নামে পরিচয় ঠিকানা তারিখ বয়স জন্ম মৃত্যু জীবন জীবনে জগৎ পৃথিবী বিশ্ব প্রকৃতি পরিবেশ গাছ ফুল ফলের পাখি পশু প্রাণী কুকুর বিড়াল ছাগল হাঁস ঘোড়া হাতি বাঘ সিংহ সাপ রাজনীতি অর্থনীতি সমাজ সংস্কৃতি আইন বিচার স্বাধীনতা গণতন্ত্র নির্বাচন ভোট দল সংসদ আদালত সেনা যুদ্ধ শান্তি সন্ত্রাস দুর্নীতি উন্নয়ন প্রযুক্তি গবেষণা আবিষ্কার ওহ আহা ইশ উফ আরে আচ্ছা ঠিকাছে ঠিকআছে হ্যালো হাই বাই টাটা হাফেজ থেকো যত্ন নিও সাবধানে অক্ষর অঙ্ক অজানা অতিথি অদ্ভুত অধিকার অনুসন্ধান অনুবাদ অন্ধকার অন্তর অপরাধ অভিজ্ঞতা অভিনেতা অভিনেত্রী অভ্যাস অর্থ অলংকার অসম্ভব অস্ত্র অস্থির আকার ")
        sb.append("আখ আগ্রহ আঙ্গুল আচরণ আজান আদর আদেশ আধুনিক আন্দোলন আবহাওয়া আমন্ত্রণ আয়না আয়োজন আরাম আলো আলোচনা আশীর্বাদ আশ্চর্য আসন আসবাব আসামি আসল ইট ইন্টারভিউ ইলিশ ইস্তফা ঈগল ঈমান ঈদগাহ উকিল উচিত উচ্চ উত্তম উদ্যোগ উদ্ধার উন্নতি উপকার উপন্যাস উপস্থিত উপাসনা উৎপাদন উৎসাহ উল্টো উষ্ণ ঋতু একাকী একাদশ একুশ এগিয়ে এজেন্ট এলাকা এলাকায় এসএমএস এসি ঐতিহ্য ঐক্য ওজন ওড়না ওয়েবসাইট ওষুধের কচ্ছপ কন্যা কপাল কবর কবি কবিতা কম্বল কয়লা কর কর্ম কর্মচারী কর্মী কষ্ট কাউকে কাঁচা কাঁচি কাঁটা কাঁদা কাছ কাজল কাটা কাপড় কাফন কাঠ কামরা কার্যক্রম কাহিনি কিছুক্ষণ কিনারা কিশোর কিশোরী কীর্তি কুরবানি কূটনীতি কৃষি কেক কেন্দ্র কেরানি কেল্লা কোট কোমর কোমল কৌতুক ক্যালেন্ডার ক্রমে ক্রিকেট ক্লান্তি ক্ষতি ক্ষমতা ক্ষুধা ক্ষেত খড় খনি খবরের খাদ্য খাল খিদে খুঁজি খুঁজে খুন ")
        sb.append("খেলনা খেলোয়াড় খোকা খোঁজ গতি গন্ধ গর্ব গল্প গহনা গায়ক গায়িকা গালি গিটার গুণ গুরুত্ব গুজব গুলি গোপন গোসল গোল গ্যাস গ্রন্থ গ্রহণ গ্রামীণ ঘটনা ঘড়ি ঘন ঘাট ঘাম ঘাস ঘুড়ি ঘুরে ঘোষণা চক্র চশমা চাকা চাকর চাদর চাপ চামচ চাল চাষ চাহিদা চিঠি চিতা চিত্র চিরকাল চিৎকার চুক্তি চুমু চুরি চেয়ার চেহারা চোর চৌকি চ্যালেঞ্জ ছক ছড়া ছাতা ছায়া ছুরি ছেলেমেয়ে ছোটবেলা ছোলা জখম জন জনপ্রিয় জবাব জমি জয় জরিমানা জলদি জাতীয় জাদু জায়গা জামা জাল জিজ্ঞাসা জিনিস জিন্স জুতা জুস জ্বালানি ঝগড়া ঝরনা ঝুঁকি ঝোল টানা টিভি টুকরো টেবিল টেলিফোন টেস্ট ঠাণ্ডা ঠোঁট ডাক ডাকাত ডাকঘর ডিগ্রি ডিজিটাল ডুব ডেস্ক ঢাকনা ঢেউ তথ্য তদন্ত তরুণ তরুণী তলা তাপমাত্রা তালা তিথি তুলনা তুলা তৃষ্ণা তৈরি থানা থালা থিয়েটার দক্ষ ")
        sb.append("দড়ি দর্শক দলিল দাড়ি দাবি দারোয়ান দিক দিগন্ত দিনরাত দীর্ঘ দূর দূরত্ব দৃশ্য দেওয়াল দেয়াল দ্রব্য দ্বীপ ধর্ম ধান ধুলো ধোঁয়া নকশা নগর নবী নাগরিক নাচ নাতি নাতনি নায়ক নায়িকা নারকেল নালিশ নিঃশ্বাস নিচ নিয়ম নিয়ন্ত্রণ নিরাপত্তা নিশ্চিত নীতি ন্যায় পকেট পক্ষ পছন্দ পড়শি পত্র পত্রিকা পথ পথিক পদ পদক্ষেপ পরিবর্তন পরিবহন পরিমাণ পর্দা পর্যটক পাখা পাড়া পার্থক্য পাসপোর্ট পিতা পুকুর পুরস্কার পুস্তক পূজা পেনসিল পেশা পোশাক প্যাকেট প্রকল্প প্রকাশ প্রচার প্রতিদিন প্রতিযোগিতা প্রতিষ্ঠান প্রত্যেক প্রদর্শনী প্রধান প্রবন্ধ প্রভাব প্রশংসা প্রস্তুতি প্রার্থনা প্রিয় প্লেট ফটো ফসল ফাঁকা ফি ফিতা ফুলের ফেরি ফ্যান ফ্রিজ বইয়ের বক্তৃতা বন্যা বন্দর বরফ বর্ণ বর্তমান বাক্য বাজে বাঁশি বাতি বাদাম বান্ধবী বাবু বায়ু বার বারান্দা বিক্রয় বিজয় বিজ্ঞাপন বিদেশ বিদ্যুৎ বিনামূল্যে বিবরণ বিরতি বিশেষ বিস্তার ")
        sb.append("বিস্কুট বীজ বুদ্ধি বুধ বৃদ্ধ বেলা বোতল ব্যক্তি ব্যবহার ব্যাগ ব্যাপার ভক্ত ভবন ভরসা ভাগ ভাগ্য ভাঙা ভাড়া ভাব ভাষা ভিতর ভিসা ভূমি মঞ্চ মন্তব্য মন্দ মশা মহান মানচিত্র মাপ মায়া মাসিক মিলন মুক্ত মুদি মুদ্রা মেজাজ মেলা মেহমান মোড় মোম মোজা মৌসুম যন্ত্র যাত্রী যাদুঘর যানবাহন যুক্তি যুবক যুবতী যোগ যোগাযোগ যোগ্য রং রচনা রাজ রাজা রাজধানী রানি রায় রূপ রেখা রেজাল্ট রেস্তোরাঁ রোগী লক্ষণ লাইন লাভ লিখিত লেবু লেখক লেখিকা লোকসান শক্তি শখ শত্রু শব্দ শহরের শান্ত শাড়ি শিক্ষার্থী শিখা শিল্প শিল্পী শীতল শুক্র শূন্য শেখা শোক শৌখিন শ্রম সংখ্যা সংগীত সঙ্গী সচেতন সঠিক সতর্ক সদস্য সফর সভা সমান সম্পর্ক সম্পাদক সম্মান সহায়তা সাংবাদিক সাজ সাধারণ সাবান সাহস সেবা সোনা সৌন্দর্য স্থান স্নান স্বর স্বাধীন স্বাগত হজ হীরা ")
        sb.append("হুজুর হেড হ্যান্ডসাম আস্তাগফিরুল্লাহ জাযাকাল্লাহ বারাকাল্লাহ আমীন ইফতার সেহরি তারাবি তাহাজ্জুদ ফজর যোহর আসর মাগরিব এশা জানাজা কবরস্থান মক্কা মদিনা কাবা হাজি ওমরা যাকাত ফিতরা ঈদুল ফিতর আযহা শবে বরাত কদর মিলাদ খুতবা ইমাম মুয়াজ্জিন কাদের কিসের কিসে কীসের কীসে কেমনে কোনদিকে কতদূর কতক্ষণ কতদিন কতবার কতজন কবে কবেই কখনোই কোনোদিন কোনোভাবে কোনোমতে যেভাবে সেভাবে এভাবে ওভাবে এমন এমনকি তবুও তবুই তাছাড়া তাছাড়াও এছাড়া এছাড়াও অন্তত অন্তরে প্রথমে দ্বিতীয়ত তৃতীয়ত সবশেষে অবশেষে নিশ্চয় বোধহয় করতেছি করতেছো করতেছেন যাইতেছি আসতেছি খাইতেছি দেখতেছি বলতেছি কইরা গিয়াছি হইছে হইয়া আইছি যাইমু খামু করমু দেখমু কইছি গুডমর্নিং গুড নাইট ইভিনিং থ্যাঙ্কস থ্যাঙ্ক ইউ সরি ঘুমিয়েছো ঘুমিয়েছেন আসোনি আসেননি ভালোই ভালোয় মারাত্মক ভয়ংকর ভীষণ প্রচণ্ড তীব্র অতিরিক্ত পর্যাপ্ত যথেষ্ট কিঞ্চিৎ সামান্য অল্প সামান্যই বিস্তর প্রচুর ")
        sb.append("অগণিত ")
        sb.append("কিরে কিরকম কিরূপ দোস্ত দোস্তি ভাইজান আপুমনি বোনটা ভাইটা মামু মামী চাচু চাচী খালামনি ফুপু দাদু নানু নানী দাদী বৌ জামাই ভাবী দেবর ননদ বেয়াই ভাতিজা ভাতিজি ভাগ্নে ভাগ্নি গুডবাই শুভসকাল শুভদুপুর শুভসন্ধ্যা আসুন বসুন বসো থামুন দাঁড়ান দাঁড়াও ধরুন হয়তবা মাঝেমধ্যে প্রায়ই সর্বদা সবসময়ই এখনো এখনও আবারও বারবার একবার দুইবার তিনবার একদম একেবারে মোটেই মোটামুটি বরং বরঞ্চ নইলে নাহলে অথচ যদিও যেহেতু যেন যতক্ষণ ততক্ষণ ঠিকই ঠিকঠাক অপূর্ব জটিল কুৎসিত সেরা সবচেয়ে সর্বোত্তম অনন্য স্বাভাবিক অস্বাভাবিক সমাচার জানাবেন জানিও জানিয়ে পড়বে পড়বেন লিখবে লিখেছেন পাঠাই পাঠাও পাঠান পাঠাচ্ছি পাঠাবো পাঠিয়েছি পাঠিয়েছো পাঠিয়েছেন পাঠিয়ে পাঠানো খেলছি খেলছো খেলছেন খেলবো খেলবে খেললাম খেলেছি হাসছি হাসছো হাসবো হাসলাম হেসে কাঁদছি কাঁদছো কাঁদবো কাঁদলাম কেঁদে গাইছি গাইবো গাইলাম গেয়ে নাচছি নাচবো নাচলাম নেচে ঘুরছি ঘুরবো ")
        sb.append("ঘুরলাম ঘোরা বেড়াতে বেড়াই বেড়াবো বেড়ালাম বেড়িয়ে বেড়ানো চড়ছি চড়বো চড়লাম চড়ে চড়া নামছি নামবো নামলাম নেমে নামা উঠছি উঠবো উঠলাম উঠে ওঠা বসছি বসবো বসলাম বসে বসা কিনছি কিনবো কিনলাম কিনে বেচছি বেচবো বেচলাম বেচে দর কমদাম বেশিদাম ছাড় অফার কমিশন হিসেব বাকি বাকী নগদ ধার সুদ ব্যাংকে একাউন্ট বিকাশ রকেট ডলার রুপি দই ঘি মাখন পনির লুচি পুরি নান কাবাব বিরানি তেহারি পোলাও ভুনা ভর্তা ভাজি ভাজা পটল ঝিঙে করলা কচু মূলা শাক পালং লালশাক পুঁইশাক কলমি আদা কাঁচামরিচ ধনিয়া জিরা গুড় মধু সরিষা রুই কাতলা পাবদা চিংড়ি কই মাগুর শিং তেলাপিয়া পাঙাশ বোয়াল জাম বাঙ্গি আঙ্গুর খেজুর ডালিম আমড়া জলপাই কুল বরই তেঁতুল কামিজ সালোয়ার পাঞ্জাবি লুঙ্গি ফতুয়া গেঞ্জি শার্ট টিশার্ট প্যান্ট পায়জামা টুপি জুতো স্যান্ডেল চটি আংটি হার চুড়ি দুল রুমাল গামছা তোয়ালে ভুরু জিভ নখ হাঁটু গোড়ালি হাড় চামড়া দেহ গায়ে ডাক্তারখানা ")
        sb.append("ক্লিনিক ফার্মেসি ট্যাবলেট সিরাপ ইনজেকশন টিকা ভ্যাকসিন সর্দিজ্বর পেটব্যথা দাঁতব্যথা কোমরব্যথা বমি ডায়রিয়া আমাশয় অম্বল এসিডিটি প্রেসার ডায়াবেটিস অ্যালার্জি ক্যান্সার সংক্রমণ নিরীক্ষা অপারেশন আরোগ্য সেরে সারবে সুস্থতা ফ্ল্যাট শোবার উঠোন গেট মেঝে সিঁড়ি লিফট খাট বিছানা বালিশ আলমারি সোফা লাইট চুলা সিএনজি অটো মোটরসাইকেল ট্রাক জাহাজ এয়ারপোর্ট টার্মিনাল সিট জ্যাম যানজট সড়ক মহাসড়ক ব্রিজ সেতু ফুটপাথ চৌরাস্তা রিসোর্ট বুকিং ভার্সিটি ক্লাসরুম লেকচার গ্রেড অ্যাসাইনমেন্ট হোমওয়ার্ক সিলেবাস কোর্স সার্টিফিকেট ভর্তি বৃত্তি স্যার ম্যাডাম প্রফেসর অধ্যক্ষ প্রধানশিক্ষক সহপাঠী রাবার স্কেল লাইব্রেরি পাঠাগার কাজকর্ম ক্যারিয়ার মাইনে রোজগার আবেদন আবেদনপত্র প্রার্থী কোম্পানি শপিং মল গ্রাহক মালিক ম্যানেজার বস সহকর্মী স্মার্টফোন চার্জার ব্যাটারি চার্জ হেডফোন ইয়ারফোন স্পিকার স্ক্রিন কিবোর্ড মাউস প্রিন্টার ")
        sb.append("রাউটার সিগন্যাল ডাটা ব্রাউজার লিংক অ্যাপস সফটওয়্যার আপডেট ইনস্টল ব্যাকআপ রিস্টার্ট ইউজার লগইন লগআউট সাইনআপ জিমেইল ইনস্টাগ্রাম মেসেঞ্জার টিকটক টুইটার টেলিগ্রাম স্ন্যাপচ্যাট লিংকডইন নেটফ্লিক্স স্পটিফাই জুম ফলোয়ার সাবস্ক্রাইব চ্যানেল লাইভ ভিডিওকল অডিও সেলফি স্ক্রিনশট ইমোজি স্টিকার মিম রিল স্টোরি স্ট্যাটাস প্রোফাইল ইনবক্স নোটিফিকেশন গ্রুপ চ্যাট মিসডকল রিংটোন সাইলেন্ট ভাইব্রেট ব্লুটুথ হটস্পট ভাইরাল ট্রেন্ডিং হ্যাশট্যাগ বৃষ্টিতে মেঘলা ঝড়ো বজ্র বজ্রপাত শিশির খরা ভূমিকম্প ঘূর্ণিঝড় নদীর বিল হাওর মরুভূমি সৈকত খামার ভেড়া বানর খরগোশ টিয়া কবুতর কাক চড়ুই প্রজাপতি মৌমাছি পিঁপড়া মাকড়সা মাছি ব্যাঙ হরিণ শিয়াল নেকড়ে ভালুক কুমির ডলফিন তিমি ফুটবল ব্যাডমিন্টন টেনিস ভলিবল হকি দাবা ক্যারম লুডু সাঁতার দৌড় সাইক্লিং ম্যাচ কোচ রেফারি স্কোর উইকেট রান ওভার টুর্নামেন্ট লিগ কাপ ফাইনাল চ্যাম্পিয়ন বিজয়ী জিত ")
        sb.append("পরাজয় ড্র সমর্থক স্টেডিয়াম অধিনায়ক চলচ্চিত্র সিরিজ পর্ব পরিচালক প্রযোজক ব্যান্ড কনসার্ট অ্যালবাম সাহিত্য নৃত্য আড্ডা বিয়েবাড়ি দাওয়াত নিমন্ত্রণ সারপ্রাইজ মজাদার আনন্দিত আঁকা নামাজে কুরআন হিন্দু খ্রিষ্টান বৌদ্ধ দুর্গাপূজা দীপাবলি বড়দিন পহেলা বিজয়দিবস একুশে দিবস অপছন্দ ঘৃণা মিস মিসিং ভয়ে দুশ্চিন্তা টেনশন উদ্বিগ্ন অবসন্ন একাকিত্ব হিংসা সৎ চালাক বুদ্ধিমান বোকা পাগল রাগী বিরক্তি নম্র ভদ্র ভদ্রতা নিষ্ঠুর দয়ালু মমতা স্নেহ মনখারাপ হতাশ হতাশা বিস্মিত বিস্ময় চমকে হতবাক বিভ্রান্ত কৃতজ্ঞ স্বস্তি উত্তেজিত কৌতূহল ভাগ্যবান দুর্ভাগ্য সৌভাগ্য আসলাম আসলে পৌঁছেছি পৌঁছাবো পৌঁছে পৌঁছানো ফিরছি ফিরলাম ফেরত ঢুকছি ঢুকবো ঢুকলাম ঢুকে বেরোচ্ছি বেরোবো বেরিয়ে বেরিয়েছি বের ধুচ্ছি ধুবো ধুয়ে ধোয়া ধুয়েছি মুছি মুছে মুছুন পরিস্কার ময়লা গোছানো গুছিয়ে সাজানো সাজাও সাজিয়ে ভাঙছি ভেঙে ভাঙ্গা ফেলছি ফেলবো ফেলে ")
        sb.append("ফেলা ছুড়ে ধরেছি তুলছি তুলবো তুলে তোলা বহন বয়ে ঢালা ঢালছি ভরা ভরছি ভরে খালি উঠেছি উঠেছো জাগছি জাগবো জেগে জাগা ঘুমাচ্ছি ঘুমাচ্ছো ঘুমাবো ঘুমালাম ঘুমিয়ে ঘুমন্ত স্বপ্নে গোসলে ব্রাশ সাজছি পরছি পরবো পরা পরেছি চাইছি চাইনা চাইনি লাগবে লাগবেনা লাগছে লাগে লাগলো লাগল লাগি পারিনি পারিনা পারেনি পারবোনা পারবেনা হবেনা হয়না হয়নি হইনি হোন আছেনা নাই নাইকো নাকি নয়তো নতুবা নেইকো থাকুন কিছুটা কিছুদিন কেউকেউ কারো কারও কাউকেই সবার সবখানে সবাইকে সকল সমস্ত প্রতিটি প্রত্যেকে প্রতিবার প্রতিবছর প্রতিমাসে প্রতিসপ্তাহে একখানা একটুখানি একসঙ্গে একই দুটো দুজনে দুজনেই ছয়টি সাতটি আটটি নয়টি দশটি পুরো পুরোটা সম্পূর্ণ অংশ দক্ষিণ পূর্ব পশ্চিম ডান বাম বামে ডানে পিছনে মাঝখানে আশেপাশে চারপাশে এপাশে ওপাশে সোনালি রুপালি রঙ গাঢ় উজ্জ্বল সকালে দুপুরে বিকেলে সন্ধ্যায় ভোরে মধ্যরাত আজকাল আজকের কালকের গতকালের পরশুদিন তরশু মুহূর্ত ")
        sb.append("অভিযোগ সাফল্য ত্রুটি দোষ সমর্থন বিরোধ আলাপ কথায় জিনিসপত্র মালপত্র স্যরি শুকরিয়া মেহেরবানি কৃপা অনুমতিক্রমে দয়াকরে মনেহয় মনেহচ্ছে মনেপড়ে মনেরাখো মনেরাখবেন যেও ভুলিনি ভুলবো গেছি ভুলিস তুই তোর তোকে তোদের তোরা আমাকেও তোমাকেও আপনাকেও তাকেও এমনও কইছো কইতাম কমু কইবো কয় কও কন কইলাম কইয়া যাইতাম যামু যাইবো আইছো আইছেন আইবো আইলাম আইয়া খাইছো খাইছেন খাইলাম খাইয়া খাইবো দেখিনি দেখাবো দেখাও দেখান দেখাচ্ছে দেখাচ্ছি করেনি করিনি করিনা করবোনা করবেনা করোনা বলতাম বলেনি বলিনি বলিনা বলবোনা বলোনা বলিস গেছো গেছেন গেছে যাইনি যাইনা যাবোনা যাবেনা যেওনা খেতাম খাইনি খাইনা খাবোনা খেওনা নেয়া নিতাম নেইনি নিইনি নিবো নিবেন নিস দেয়া দিতাম দেইনি দিইনি দিবো দিবেন দিস হারিয়েছি হারিয়ে হারানো হারাবো পেয়েছি পেয়েছো পেয়েছেন পেয়ে পাচ্ছি পাচ্ছো পাবো পাবে পাবেন পেলাম পেলে পাওয়া পাইনি পাইনা পাইছি পাইলাম ")
        sb.append("অকারণে অগ্রসর অতীত অভিযান অহংকার আওয়াজ আকাশি আঘাত আচার আড়াল আনন্দময় আন্তরিক আপস আবেগ আমদানি আরম্ভ আর্থিক আলাদা আশপাশ আস্থা ইচ্ছে ইনকাম ইমারত ইশারা ইস্তিরি উইকএন্ড উঁচু উপদেশ উপসংহার এক্সাম এজন্য এতদিন এতক্ষণ এতটুকু এতগুলো এমনিতে এলোমেলো ওয়াদা ওয়ার্ড ওপর ওপরে ওঠানামা কঠোর কথোপকথন কনফার্ম কবুল কমিটি করুণা কর্তব্য কর্মসূচি কিস্তি কেনাকাটা কেবল ক্ষুদ্র ক্ষেত্র খাম খুঁজছি খুনসুটি খোলাখুলি খ্যাতি গড়ে গণ্ডগোল গভীর গরিব গল্পগুজব গান্ধী গাছপালা গামলা গৃহ গেম গোলমাল গ্যারেজ ঘনিষ্ঠ ঘরবাড়ি ঘুমানো ঘুরাঘুরি চমক চরিত্র চালক চিন্তাভাবনা চুপ চুপচাপ চেনা চেয়ারম্যান চোখে ছাত্রজীবন ছাপা ছুটিতে ছোটখাটো ছোটো জোরে জ্যাকেট ঝুলন্ত টিম টেবিলে টেলিভিশন টাকাপয়সা ট্রেনে ঠিকমতো ঠেলা ডাক্তারি ডিসকাউন্ট ডেলিভারি ডোজ তালিকা তিক্ত তোমরাও থিম দরকারি দরজায় দলবল দিনকাল দিনভর দিশা দুঃসময় দুর্ঘটনা দুর্বল দেশপ্রেম ")
        sb.append("দোকানে দ্বিধা ধরনের ধাপ ধৈর্য নকল নতুনভাবে নম্বরে নামকরা নিজেই নিয়মিত নির্ভর নিশ্চিন্ত নীরব নোট নোটিশ পরিচিত পাওনা পাগলামি পারিবারিক পালা পিছিয়ে পুরানো পূর্ণ প্রতিজ্ঞা প্রথমবার প্রদান প্রয়োজনীয় প্রস্তাব প্রস্তুত প্রাণ প্রেম ফিরতি ফেসবুকে ফোনে ফ্রি বক্তব্য বড়ো বদল বন্ধুবান্ধব বরাবর বসন্তকাল বাঙালি বাড়তি বাড়িতে বাধা বাবার বিকল্প বিদায় বিনোদন বিয়ের বিশ্বাসী বিষয়ে বিস্তারিত বেকার বেঁচে বেরোনো বেলায় বোনের ব্যবসায় ব্যস্ততা ব্যাংকের ভক্তি ভাগ্যক্রমে ভালোমন্দ ভিড় ভিন্ন বোঝাবুঝি ভূমিকা ভেজা ভ্রমণে মনোযোগ মাঝেমাঝে মানে মায়ের মাসে মাস্ক মিছিল মিনিটে মিলে মুখে মূল্যবান মেয়েটি মোবাইলে মোটর যতদিন যতবার যথাসময়ে যাত্রায় রক্ষা রঙিন রবিবারে রসিকতা রহস্য রাত্রি রাতারাতি রাস্তায় রিকশায় রিপোর্টে রেজাল্টে রোজ রোদ্দুর লজ্জিত লাগাতার লাভজনক লিস্ট লুকিয়ে লেখাপড়া লেনদেন শখের শরীরে শীঘ্র ")
        sb.append("শুক্রবারে শুধুই কামনা শৈশব শ্রদ্ধা সংগ্রাম সংবাদপত্র সংসার সকলকে সততা সপ্তাহে সম্পদ সম্মুখে সময়মতো সরল সহজেই সাথি সাবধান সামনাসামনি সার্ভিস সিঁড়িতে সিনেমায় সিস্টেম সীমা সুনাম সুবিধাজনক সুস্বাদু সোজা স্কুলে স্থায়ী স্পষ্ট স্বপ্নের স্বভাব স্বাস্থ্যকর হঠাৎ হতাশাজনক হরতাল হাজারো হাতে হাসপাতালে হিসাবে হুট হৃদয়ে হোটেলে ")
        sb.append("কালকে গতরাতে আজরাতে সকালবেলা মাঝরাতে একশো খাওয়ার খাওয়াদাওয়া খাবারের যাওয়ার আসার দেখার বলার দেন দেওয়ার নেন নেওয়ার পড়লেন পড়েছেন পড়ার লিখলেন লিখবেন লেখার শুনছো শুনছেন শুনছে শুনলেন শুনবে শুনবেন শোনার জানলেন জেনেছেন জানবেন জানার জানিয়েছি বুঝলেন বুঝবে বোঝার চাইবে চাওয়ার পারলেন পারার থাকলেন থাকার ভাবেন ভাবে ভাবছো ভাবছেন ভাবছে ভাবলাম ভাবলে ভেবেছি ভেবেছো ভেবেছেন ভেবেছে ভাববো ভাববে ভাবতে ভাবা খোঁজো খুঁজছো খুঁজছেন খুঁজছে খুঁজলাম খুঁজেছি খুঁজবো খুঁজতে খোঁজা ধরেন ধরে ধরছি ধরলাম ধরবো ধরতে রাখেন রাখে রাখছি রাখলাম রেখেছি রেখেছো রেখেছেন রাখবো রাখবে রাখতে রেখে ছাড়েন ছাড়ে ছাড়ছি ছাড়লাম ছেড়েছি ছাড়বো ছাড়তে খোলো খোলেন খোলে খুলছি খুললাম খুলেছি খুলবো খুলতে খুলে বন্ধুদের বন্ধুর কেনো কেনেন কিনতে বেচি বাজারে দামে টাকার বসি বসেন বসেছি বসতে দাঁড়াই দাঁড়িয়ে দাঁড়াচ্ছি দাঁড়িয়েছি দাঁড়াবো দাঁড়াতে শুই শোও শোন শুয়ে শুচ্ছি শুলাম শুয়েছি শুবো শুতে ঘুমাই ঘুমাও ঘুমান ঘুমায় ঘুমিয়েছি ঘুমাতে ঘুমে উঠি ওঠো ওঠেন উঠতে নামি নামো নামেন নেমেছি নামতে হাসো হাসেন হাসে হেসেছি হাসতে হাসা কাঁদি কাঁদো কাঁদেন কাঁদে কেঁদেছি কাঁদতে খেলি খেলতে খেলার খেলাধুলা গাই গাও গায় গেয়েছি গাইতে গাওয়া গানের সুর ভালোবাসে ভালোবাসছি ভালোবাসলাম ভালোবেসেছি ভালোবাসবো ভালোবাসার ভালোবেসে প্রেমিক প্রেমিকা মিস্ ভুলি ভুলো ভুলেছি ভোলা ভুলের ")
        sb.append("চাকরির অফিসে অফিসের মেইল ঠিকানায় নাম্বার ফোনের সিম রিচার্জ ব্যালেন্স আইডি সাইন ফোল্ডার ছবিটা ছবির ভিডিওটা গানটা ইমো আব্বা ভাইয়ের খালু কাকা কাকি জেঠা জেঠি ভাগনা ভাগনি ছেলেরা মেয়েরা ছেলের মেয়ের বর পরিবারের আত্মীয়স্বজন ঘাড় পায়ে হৃদপিণ্ড ত্বক পরাটা কাচ্চি গোশত ঝিঙা মশলা ধনেপাতা পুদিনা রসগোল্লা সন্দেশ শরবত সকালের তৃপ্তি ক্ষুধার পিপাসা তেষ্টা স্বাদ নোনতা রঙের চওড়া সরু নিচু ধীর শুকনো কড়া তাজা বাসি ধনী গরীব মিথ্যে সত্যিকারের কঠিনভাবে খুশিতে দুঃখে কষ্টে ভীত আনন্দে অশান্তি একাকীত্ব অবসাদ অভিমান বিশ্বাসঘাতক আশায় সাহসী মুগ্ধ কৃতজ্ঞতা সম্মানিত ক্লাসে পরীক্ষায় পাস ফেল ব্যাগে বোর্ড পাঠ্য অধ্যায় রসায়ন পদার্থবিজ্ঞান জীববিজ্ঞান ভর্তির ঔষধ ব্যথায় চিকিৎসক স্বাস্থ্যের হাঁটা যোগব্যায়াম গাড়িতে স্টপেজ রাস্তার মোড়ে ভাড়ায় পথে বাড়ির বাসায় বাসার ঘরে ঘরের রুমে ছাদে লেপ চাবি শহরে গ্রামের দেশে দেশের বিদেশে উপজেলা ইউনিয়ন মহল্লা মাঠে গাছে পাতা রোদে ঝড়ে শীতে তারকা জোছনা নরসিংদী টাঙ্গাইল ফরিদপুর যশোর দিনাজপুর নোয়াখালী ফেনী চাঁদপুর বাংলাদেশের ভুটান শ্রীলঙ্কা লন্ডন অস্ট্রেলিয়া আরব সৌদিআরব দুবাই কুয়েত কাতার মালয়েশিয়া সিঙ্গাপুর আল্লাহর রাসুল মুহাম্মদ হাদিস ইসলামিক মুসলমান রোজার হজ্জ দোয়ার মসজিদে জুম্মা জুমার ঈদের শাবান মুহাররম শবেবরাত সদকা দান জান্নাত জাহান্নাম গুনাহ সওয়াব তওবা রহমত বরকত নেয়ামত ")
        sb.append("আল্লাহু আকবার আসসালাম ইয়া মুবারক পূজার বুদ্ধ উৎসবে মেলায় অনুষ্ঠানে বিবাহের বধূ দাওয়াতে জন্মদিনে জন্মদিনের বার্ষিকী আড্ডায় গল্পের নাটকের খেলায় দলের জিতেছি হেরেছি সরকারি রাষ্ট্র ভোটার মন্ত্রণালয় থানায় ঘুষ মুক্তিযুদ্ধ মুক্তিযোদ্ধা শহীদ পতাকা জাতির জনক লোন সঞ্চয় জমা জমানো খরচের আয়কর পুঁজি বিনিয়োগ বিকাশে নগদে পেমেন্ট পেমেন্টের ক্যাশ বিলের বকেয়া আগামীকালের এখনকার তখনকার কদাচিৎ সবকিছুই সকলের পুরোপুরি আংশিক অনেকে অনেকটা অনেকগুলো কয়েক কয়েকটি কয়েকজন মোটেও তাইতো সুতরাং অতএব তথাপি কিংবা ব্যতীত ছাড়াও সাথেও পাশাপাশি পরেও আগেও তখনই সঙ্গেই ধীরেধীরে আচমকা সবেমাত্র সদ্য এইমাত্র ধন্যবাদান্তে সুস্বাগতম শুভেচ্ছান্তে শুভদিন শুভবিদায় খেয়াল রেখো রাখবেন হেই লেগেছে হচ্ছিল হতাম যায়নি আসেনি আসিনি জানিনা নও নন নহে ট্রেন্ড ভয়েস গ্রুপে পেজ পেজে ব্লক সেটিং কীবোর্ড ")
        sb.append("হুম খুবই মাত্র সেজন্য ওবার হালিম সেমাই চমচম পাঙাস ডাব নারিকেল চানাচুর ফুচকা চটপটি সিঙ্গারা সমুচা বার্গার পিৎজা স্যান্ডউইচ নুডলস চাউমিন নাশতা লবণাক্ত লক্ষ্মীপুর পটুয়াখালী কুষ্টিয়া পাবনা সিরাজগঞ্জ নাটোর ঠাকুরগাঁও জামালপুর বান্দরবান রাঙামাটি খাগড়াছড়ি হবিগঞ্জ মৌলভীবাজার সুনামগঞ্জ গোপালগঞ্জ মাদারীপুর শরীয়তপুর মানিকগঞ্জ কিশোরগঞ্জ নেত্রকোনা শেরপুর সাতক্ষীরা বাগেরহাট নড়াইল মাগুরা ঝিনাইদহ চুয়াডাঙ্গা মেহেরপুর পিরোজপুর ঝালকাঠি বরগুনা লালমনিরহাট কুড়িগ্রাম গাইবান্ধা নীলফামারী পঞ্চগড় জয়পুরহাট নওগাঁ চাপাইনবাবগঞ্জ মিরপুর উত্তরা গুলশান বনানী ধানমন্ডি মতিঝিল মোহাম্মদপুর যাত্রাবাড়ী ")
        sb.append("বাড্ডা রামপুরা মালিবাগ ফার্মগেট শাহবাগ গাবতলী সায়েদাবাদ কমলাপুর আজিমপুর কলাবাগান তেজগাঁও মগবাজার পল্টন গুলিস্তান সদরঘাট টঙ্গী আশুলিয়া সাভার কেরানীগঞ্জ প্যাকেজ ম্যাসেজ ট্যাব লিঙ্ক সার্চ অর্ডার পণ্য শুদ্ধ ভাল চিকন পুরাতন তাড়াহুড়া ফরম সনদ ভাগনে দুলাভাই পায়ের হার্ট চেকআপ বালু পাট রেল উবার আযান ওযু অজু সালাত জুমআ তারাবিহ সেহেরি সুন্নাত ফরজ ওয়াজিব হালাল হারাম দুনিয়া আখিরাত কিয়ামত রাসূল সাহাবী ফেরেশতা শয়তান খাইরান আল্লাহ্ মাগফিরাত হিদায়াত তাওফিক বুদ্ধপূর্ণিমা নববর্ষ মামলা আনসার ")
        sb.append("বল ব্যাট বিশ্বকাপ সিরিয়াল মুভি ওয়েব বিবাহবার্ষিকী হুমম ওহো ছি বাহ বাহ্ হায় হায়রে আরেহ আরেকটু ধুর যাহ বাপরে ওরে ও ওগো বুঝিনি বুঝতেছি করতেছে পারতেছি পারবনা চাইছো চাইছেন লাগেনি লাগতো লাগলে লাগছি হচ্ছেনা হইসে হইলো হইবো হওয়ার হয়েছিলাম হয়েছিলো নাইরে নাহ নাহয় নই নেইতো করার করতো যাচ্ছিলাম গিয়েছিলাম গিয়েছিলে এসেছিলাম আসছিলাম দেখেনি বলতো বলতেন জানতাম জানতো জানিনি জানাচ্ছি জানাতে ধরছো ধরছেন ধরছে ধরলে ধরলেন ধরল ধরলো ধরবে ধরবেন ধরতাম ")
        sb.append("ধরতো ধরতেন ধরেছো ধরেছেন ধরেছে ভাবলেন ভাবল ভাবলো ভাববেন ভাবতাম ভাবতো ভাবতেন ভাবেছি ভাবেছো ভাবেছেন ভাবেছে ভাবুন চলছো চলছেন চললাম চললে চললেন চলল চললো চলবো চলবেন চলতাম চলতো চলতেন চলেছি চলেছো চলেছেন চলেছে বসছো বসছেন বসছে বসলে বসলেন বসল বসলো বসবে বসবেন বসতাম বসতো বসতেন বসেছো বসেছেন বসেছে থামছি থামছো থামছেন থামছে থামলাম থামলে থামলেন থামল থামলো থামবো থামবে থামবেন থামতে থামতাম থামতো থামতেন থামেছি থামেছো থামেছেন থামেছে রাখছো রাখছেন ")
        sb.append("রাখছে রাখলে রাখলেন রাখল রাখলো রাখতাম রাখতো রাখতেন রাখেছি রাখেছো রাখেছেন রাখেছে শিখছি শিখছো শিখছেন শিখছে শিখলাম শিখলে শিখলেন শিখল শিখলো শিখবো শিখবে শিখবেন শিখতে শিখতাম শিখতো শিখতেন শিখেছি শিখেছো শিখেছেন শিখেছে শিখো শিখুন খুঁজলে খুঁজলেন খুঁজল খুঁজলো খুঁজবে খুঁজবেন খুঁজতাম খুঁজতো খুঁজতেন খুঁজেছো খুঁজেছেন খুঁজেছে খুঁজো খুঁজুন খুঁজা ডাকছো ডাকছেন ডাকছে ডাকলাম ডাকলে ডাকলেন ডাকল ডাকলো ডাকবো ডাকবে ডাকবেন ডাকতে ডাকতাম ডাকতো ডাকতেন ডাকেছি ডাকেছো ডাকেছেন ডাকেছে ডাকুন ঘুরছো ")
        sb.append("ঘুরছেন ঘুরছে ঘুরলে ঘুরলেন ঘুরল ঘুরলো ঘুরবে ঘুরবেন ঘুরতাম ঘুরতো ঘুরতেন ঘুরেছি ঘুরেছো ঘুরেছেন ঘুরেছে ঘুরো ঘুরুন ঘুরা কাটছি কাটছো কাটছেন কাটছে কাটলাম কাটলে কাটলেন কাটল কাটলো কাটবো কাটবে কাটবেন কাটতে কাটতাম কাটতো কাটতেন কাটেছি কাটেছো কাটেছেন কাটেছে কাটো কাটুন ভাঙছো ভাঙছেন ভাঙছে ভাঙলাম ভাঙলে ভাঙলেন ভাঙল ভাঙলো ভাঙবো ভাঙবে ভাঙবেন ভাঙতে ভাঙতাম ভাঙতো ভাঙতেন ভাঙেছি ভাঙেছো ভাঙেছেন ভাঙেছে ভাঙো ভাঙুন গড়ছি গড়ছো গড়ছেন গড়ছে গড়লাম গড়লে গড়লেন গড়ল গড়লো ")
        sb.append("গড়বো গড়বে গড়বেন গড়তে গড়তাম গড়তো গড়তেন গড়েছি গড়েছো গড়েছেন গড়েছে গড়ো গড়ুন গড়া জানছো জানছেন জানছে জানলো জানতেন জানেছি জানেছো জানেছেন জানেছে জানুন মানছি মানছো মানছেন মানছে মানলাম মানলে মানলেন মানল মানলো মানবো মানবে মানবেন মানতে মানতাম মানতো মানতেন মানেছি মানেছো মানেছেন মানেছে মানো মানুন মানা ঢালছো ঢালছেন ঢালছে ঢাললাম ঢাললে ঢাললেন ঢালল ঢাললো ঢালবো ঢালবে ঢালবেন ঢালতে ঢালতাম ঢালতো ঢালতেন ঢালেছি ঢালেছো ঢালেছেন ঢালেছে ঢালো ঢালুন বাজছি বাজছো ")
        sb.append("বাজছেন বাজছে বাজলাম বাজলে বাজলেন বাজল বাজলো বাজবো বাজবে বাজবেন বাজতে বাজতাম বাজতো বাজতেন বাজেছি বাজেছো বাজেছেন বাজেছে বাজো বাজুন বাজা ছাড়ছো ছাড়ছেন ছাড়ছে ছাড়লে ছাড়লেন ছাড়ল ছাড়লো ছাড়বে ছাড়বেন ছাড়তাম ছাড়তো ছাড়তেন ছাড়েছি ছাড়েছো ছাড়েছেন ছাড়েছে ছাড়ুন বাড়ছি বাড়ছো বাড়ছেন বাড়ছে বাড়লাম বাড়লে বাড়লেন বাড়ল বাড়লো বাড়বো বাড়বে বাড়বেন বাড়তে বাড়তাম বাড়তো বাড়তেন বাড়েছি বাড়েছো বাড়েছেন বাড়েছে বাড়ো বাড়ুন বাড়া কাঁদছেন কাঁদছে কাঁদলে কাঁদলেন কাঁদল কাঁদলো কাঁদবে কাঁদবেন কাঁদতাম ")
        sb.append("কাঁদতো কাঁদতেন কাঁদেছি কাঁদেছো কাঁদেছেন কাঁদেছে কাঁদুন হাসছেন হাসছে হাসলে হাসলেন হাসল হাসলো হাসবে হাসবেন হাসতাম হাসতো হাসতেন হাসেছি হাসেছো হাসেছেন হাসেছে হাসুন নাচছো নাচছেন নাচছে নাচলে নাচলেন নাচল নাচলো নাচবে নাচবেন নাচতে নাচতাম নাচতো নাচতেন নাচেছি নাচেছো নাচেছেন নাচেছে নাচো নাচুন নাচা চালছি চালছো চালছেন চালছে চাললাম চাললে চাললেন চালল চাললো চালবো চালবে চালবেন চালতে চালতাম চালতো চালতেন চালেছি চালেছো চালেছেন চালেছে চালো চালুন চালা সাজছো সাজছেন সাজছে সাজলাম ")
        sb.append("সাজলে সাজলেন সাজল সাজলো সাজবো সাজবে সাজবেন সাজতে সাজতাম সাজতো সাজতেন সাজেছি সাজেছো সাজেছেন সাজেছে সাজো সাজুন সাজা ঝাড়ছি ঝাড়ছো ঝাড়ছেন ঝাড়ছে ঝাড়লাম ঝাড়লে ঝাড়লেন ঝাড়ল ঝাড়লো ঝাড়বো ঝাড়বে ঝাড়বেন ঝাড়তে ঝাড়তাম ঝাড়তো ঝাড়তেন ঝাড়েছি ঝাড়েছো ঝাড়েছেন ঝাড়েছে ঝাড়ো ঝাড়ুন ঝাড়া মাপছি মাপছো মাপছেন মাপছে মাপলাম মাপলে মাপলেন মাপল মাপলো মাপবো মাপবে মাপবেন মাপতে মাপতাম মাপতো মাপতেন মাপেছি মাপেছো মাপেছেন মাপেছে মাপো মাপুন মাপা ফেলছো ফেলছেন ফেলছে ফেললাম ফেললে ফেললেন ")
        sb.append("ফেলল ফেললো ফেলবে ফেলবেন ফেলতে ফেলতাম ফেলতো ফেলতেন ফেলেছি ফেলেছো ফেলেছেন ফেলেছে ফেলো ফেলুন টানছি টানছো টানছেন টানছে টানলাম টানলে টানলেন টানল টানলো টানবো টানবে টানবেন টানতে টানতাম টানতো টানতেন টানেছি টানেছো টানেছেন টানেছে টানো টানুন ছোটছি ছোটছো ছোটছেন ছোটছে ছোটলাম ছোটলে ছোটলেন ছোটল ছোটলো ছোটবো ছোটবে ছোটবেন ছোটতে ছোটতাম ছোটতো ছোটতেন ছোটেছি ছোটেছো ছোটেছেন ছোটেছে ছোটুন ছোটা লাগছো লাগছেন লাগলাম লাগলেন লাগবো লাগবেন লাগতে লাগতাম লাগতেন লাগেছি লাগেছো লাগেছেন ")
        sb.append("লাগেছে লাগো লাগুন লাগা জাগছো জাগছেন জাগছে জাগলাম জাগলে জাগলেন জাগল জাগলো জাগবে জাগবেন জাগতে জাগতাম জাগতো জাগতেন জাগেছি জাগেছো জাগেছেন জাগেছে জাগো জাগুন বদলছি বদলছো বদলছেন বদলছে বদললাম বদললে বদললেন বদলল বদললো বদলবো বদলবে বদলবেন বদলতে বদলতাম বদলতো বদলতেন বদলেছি বদলেছো বদলেছেন বদলেছে বদলো বদলুন বদলা ঠেলছি ঠেলছো ঠেলছেন ঠেলছে ঠেললাম ঠেললে ঠেললেন ঠেলল ঠেললো ঠেলবো ঠেলবে ঠেলবেন ঠেলতে ঠেলতাম ঠেলতো ঠেলতেন ঠেলেছি ঠেলেছো ঠেলেছেন ঠেলেছে ঠেলো ঠেলুন বাঁচছি ")
        sb.append("বাঁচছো বাঁচছেন বাঁচছে বাঁচলাম বাঁচলে বাঁচলেন বাঁচল বাঁচলো বাঁচবো বাঁচবে বাঁচবেন বাঁচতে বাঁচতাম বাঁচতো বাঁচতেন বাঁচেছি বাঁচেছো বাঁচেছেন বাঁচেছে বাঁচো বাঁচুন বাঁচা আঁকছি আঁকছো আঁকছেন আঁকছে আঁকলাম আঁকলে আঁকলেন আঁকল আঁকলো আঁকবো আঁকবে আঁকবেন আঁকতে আঁকতাম আঁকতো আঁকতেন আঁকেছি আঁকেছো আঁকেছেন আঁকেছে আঁকো আঁকুন চাপছি চাপছো চাপছেন চাপছে চাপলাম চাপলে চাপলেন চাপল চাপলো চাপবো চাপবে চাপবেন চাপতে চাপতাম চাপতো চাপতেন চাপেছি চাপেছো চাপেছেন চাপেছে চাপো চাপুন চাপা ডুবছি ডুবছো ডুবছেন ")
        sb.append("ডুবছে ডুবলাম ডুবলে ডুবলেন ডুবল ডুবলো ডুববো ডুববে ডুববেন ডুবতে ডুবতাম ডুবতো ডুবতেন ডুবেছি ডুবেছো ডুবেছেন ডুবেছে ডুবো ডুবুন ডুবা ভাসছি ভাসছো ভাসছেন ভাসছে ভাসলাম ভাসলে ভাসলেন ভাসল ভাসলো ভাসবো ভাসবে ভাসবেন ভাসতে ভাসতাম ভাসতো ভাসতেন ভাসেছি ভাসেছো ভাসেছেন ভাসেছে ভাসো ভাসুন ভাসা ওড়ছি ওড়ছো ওড়ছেন ওড়ছে ওড়লাম ওড়লে ওড়লেন ওড়ল ওড়লো ওড়বো ওড়বে ওড়বেন ওড়তে ওড়তাম ওড়তো ওড়তেন ওড়েছি ওড়েছো ওড়েছেন ওড়েছে ওড়ো ওড়ুন ওড়া ভরছো ভরছেন ভরছে ভরলাম ")
        sb.append("ভরলে ভরলেন ভরল ভরলো ভরবো ভরবে ভরবেন ভরতে ভরতাম ভরতো ভরতেন ভরেছি ভরেছো ভরেছেন ভরেছে ভরো ভরুন খাটছি খাটছো খাটছেন খাটছে খাটলাম খাটলে খাটলেন খাটল খাটলো খাটবো খাটবে খাটবেন খাটতে খাটতাম খাটতো খাটতেন খাটেছি খাটেছো খাটেছেন খাটেছে খাটুন খাটা ")
        sb.toString().split(" ").filter { it.isNotEmpty() }.distinct()
    }

    // পরের শব্দের প্রাথমিক পরামর্শ (ব্যবহারকারীর শেখা শব্দের পরে দেখানো হয়)
    val enNext: Map<String, List<String>> = mapOf(
        "i" to "am have will would think want love need can don't".split(" "),
        "i'm" to "going not sure sorry glad fine happy".split(" "),
        "you" to "are can have will know need want should".split(" "),
        "we" to "are can have will need should".split(" "),
        "they" to "are have will were said".split(" "),
        "he" to "is was has will said".split(" "),
        "she" to "is was has will said".split(" "),
        "it" to "is was will can has".split(" "),
        "the" to "best most same first other".split(" "),
        "this" to "is was will can".split(" "),
        "that" to "is was will would".split(" "),
        "what" to "is are do about time happened".split(" "),
        "how" to "are to much many about is do".split(" "),
        "where" to "are is do you".split(" "),
        "when" to "will do are you is can".split(" "),
        "why" to "do are is not did".split(" "),
        "thank" to "you".split(" "),
        "thanks" to "for a so".split(" "),
        "good" to "morning night afternoon evening luck".split(" "),
        "happy" to "birthday new eid anniversary".split(" "),
        "see" to "you later it the".split(" "),
        "talk" to "to later soon".split(" "),
        "call" to "me you back".split(" "),
        "let" to "me us you know".split(" "),
        "please" to "send let tell help call come".split(" "),
        "do" to "you not it i we".split(" "),
        "are" to "you we they there the not".split(" "),
        "is" to "it this there that a the not".split(" "),
        "can" to "you i we please help".split(" "),
        "will" to "be you do see i".split(" "),
        "have" to "a to been you nice good".split(" "),
        "to" to "be the do see go".split(" "),
        "in" to "the a my this".split(" "),
        "on" to "the my a this".split(" "),
        "of" to "the a course my".split(" "),
        "for" to "the you a me".split(" "),
        "with" to "you me the my".split(" "),
        "my" to "name friend phone".split(" "),
        "your" to "name phone message".split(" "),
        "very" to "good much nice well".split(" "),
        "so" to "much good sorry i you we".split(" "),
        "no" to "problem way worries".split(" "),
        "not" to "sure now yet".split(" "),
        "just" to "a now wanted".split(" "),
        "wish" to "you".split(" "),
        "take" to "care a it".split(" "),
        "eid" to "mubarak".split(" "),
        "assalamualaikum" to "wa brother".split(" "),
        "i'll" to "be call see let try send".split(" "),
        "i've" to "been got seen never just".split(" "),
        "don't" to "know worry forget think have".split(" "),
        "can't" to "wait believe find remember".split(" "),
        "it's" to "a not okay fine ok".split(" "),
        "that's" to "great fine true right why a not".split(" "),
        "there" to "is are was will".split(" "),
        "going" to "to home out well".split(" "),
        "want" to "to a some you".split(" "),
        "need" to "to help some a".split(" "),
        "sorry" to "for about i".split(" "),
        "merry" to "christmas".split(" "),
        "best" to "wishes of friend regards".split(" "),
        "what's" to "up your going the".split(" "),
        "where's" to "the my your".split(" "),
        "how's" to "it your life everything".split(" "),
        "come" to "here on back home".split(" "),
        "go" to "to home on ahead".split(" "),
        "send" to "me it the".split(" "),
        "let's" to "go meet talk start see".split(" "),
        "right" to "now here away".split(" "),
        "at" to "the home work least".split(" "),
        "by" to "the now".split(" "),
        "from" to "the my home".split(" "),
        "about" to "the it you".split(" "),
        "as" to "soon well a".split(" "),
        "who" to "is are was do".split(" "),
        "could" to "you i we be".split(" "),
        "would" to "you like be have".split(" "),
        "should" to "i we be have".split(" "),
        "does" to "it he she not".split(" "),
        "did" to "you not he she".split(" "),
        "was" to "a the it not".split(" "),
        "were" to "you not the".split(" "),
        "has" to "been a to not".split(" "),
        "had" to "a to been the".split(" "),
        "and" to "i the you we".split(" "),
        "but" to "i it you we".split(" "),
        "if" to "you i we it".split(" "),
        "or" to "not you i".split(" "),
        "hello" to "how sir there".split(" "),
        "hi" to "how there".split(" "),
        "hey" to "how what there".split(" "),
        "i'd" to "like love".split(" "),
        "you're" to "welcome so not".split(" "),
        "won't" to "be".split(" "),
        "a" to "lot few little good".split(" "),
        "an" to "idea hour".split(" "),
        "much" to "better".split(" "),
        "more" to "than".split(" "),
        "than" to "i".split(" "),
        "well" to "done".split(" "),
        "like" to "to".split(" "),
        "love" to "you".split(" "),
        "miss" to "you".split(" "),
        "tell" to "me".split(" "),
        "give" to "me".split(" "),
        "look" to "at".split(" "),
        "wait" to "for".split(" "),
        "waiting" to "for".split(" "),
        "looking" to "forward for".split(" "),
        "forward" to "to".split(" "),
        "because" to "of".split(" "),
        "now" to "i".split(" "),
        "also" to "i".split(" "),
        "yes" to "i".split(" "),
        "ok" to "i".split(" "),
        "okay" to "i".split(" "),
        "sure" to "thing".split(" "),
        "last" to "night".split(" "),
        "next" to "week".split(" "),
        "today" to "is".split(" "),
        "tomorrow" to "morning".split(" "),
        "tonight" to "i".split(" "),
        "every" to "day".split(" "),
        "all" to "the".split(" "),
        "our" to "team".split(" "),
        "name" to "is".split(" "),
        "phone" to "number".split(" "),
        "number" to "is".split(" "),
        "email" to "address".split(" "),
        "get" to "back".split(" "),
        "make" to "sure".split(" "),
        "try" to "to".split(" "),
        "being" to "a".split(" "),
        "been" to "a".split(" "),
        "here" to "is are".split(" "),
        "these" to "are".split(" "),
        "those" to "are".split(" "),
        "some" to "of".split(" "),
        "many" to "people".split(" "),
        "other" to "than".split(" "),
        "after" to "that".split(" "),
        "before" to "you".split(" "),
        "since" to "i".split(" "),
        "until" to "i".split(" "),
        "while" to "i".split(" "),
        "still" to "waiting".split(" "),
        "never" to "mind".split(" "),
        "always" to "here".split(" "),
        "maybe" to "i".split(" "),
        "really" to "good".split(" "),
        "actually" to "i".split(" "),
        "probably" to "not".split(" "),
        "anyway" to "i".split(" "),
        "welcome" to "to".split(" "),
        "congratulations" to "on".split(" "),
        "hope" to "you".split(" "),
        "wishing" to "you".split(" "),
        "hoping" to "you".split(" "),
        "feel" to "free".split(" "),
        "free" to "to".split(" "),
        "keep" to "in".split(" "),
        "stay" to "safe".split(" "),
        "safe" to "and".split(" "),
        "kind" to "regards".split(" "),
        "warm" to "regards".split(" "),
        "dear" to "sir".split(" "),
        "yours" to "sincerely".split(" "),
        "walaikum" to "assalam".split(" "),
        "inshallah" to "i".split(" "),
        "alhamdulillah" to "i".split(" "),
        "mubarak" to "to".split(" "),
        "new" to "year".split(" "),
        "birthday" to "wishes".split(" "),
        "anniversary" to "wishes".split(" "),
        "lol" to "i".split(" "),
        "btw" to "i".split(" "),
        "omg" to "i".split(" "),
        "meet" to "me you".split(" "),
        "meeting" to "at".split(" "),
        "office" to "hours".split(" "),
        "home" to "now".split(" "),
        "school" to "is".split(" "),
        "work" to "at".split(" "),
        "working" to "on".split(" "),
        "text" to "me you back".split(" "),
        "coming" to "soon".split(" "),
        "gonna" to "be".split(" "),
        "wanna" to "go".split(" "),
        "bro" to "i".split(" "),
        "dude" to "i".split(" "),
        "guys" to "i".split(" "),
        "morning" to "everyone".split(" "),
        "night" to "everyone".split(" ")
    )

    val bnNext: Map<String, List<String>> = mapOf(
        "আমি" to "তোমাকে আজ ভালো এখন একটু আপনাকে".split(" "),
        "তুমি" to "কেমন কি কোথায় আমাকে কখন".split(" "),
        "আপনি" to "কেমন কি কোথায় কখন".split(" "),
        "কেমন" to "আছো আছেন আছি লাগছে".split(" "),
        "কী" to "করছো করছেন খবর".split(" "),
        "কি" to "করছো করছেন খবর".split(" "),
        "কোথায়" to "আছো আছেন যাচ্ছো থাকো".split(" "),
        "ধন্যবাদ" to "আপনাকে তোমাকে অনেক".split(" "),
        "শুভ" to "সকাল রাত্রি জন্মদিন সন্ধ্যা নববর্ষ দুপুর".split(" "),
        "ভালো" to "আছি আছো আছেন থাকো লাগছে".split(" "),
        "খুব" to "ভালো সুন্দর দুঃখিত ব্যস্ত".split(" "),
        "অনেক" to "ধন্যবাদ ভালো দিন দূর".split(" "),
        "আজ" to "কি কেমন আমি কী রাতে সকালে".split(" "),
        "কাল" to "দেখা আসবো যাবো কখন সকালে".split(" "),
        "দেখা" to "হবে হলো করবো".split(" "),
        "ঠিক" to "আছে আছি হবে".split(" "),
        "আছে" to "কি কিনা".split(" "),
        "না" to "আমি তুমি".split(" "),
        "হ্যাঁ" to "আমি ঠিক".split(" "),
        "সালাম" to "নিন নিও নিবেন".split(" "),
        "আসসালামু" to "আলাইকুম".split(" "),
        "ওয়া" to "আলাইকুম".split(" "),
        "ঈদ" to "মোবারক মুবারক".split(" "),
        "জন্মদিনের" to "শুভেচ্ছা অনেক".split(" "),
        "শুভেচ্ছা" to "রইলো জানাই".split(" "),
        "আল্লাহ" to "হাফেজ ভরসা তোমাকে ভালো রাখুক".split(" "),
        "ইনশাআল্লাহ" to "দেখা কাল আসবো হবে".split(" "),
        "দয়া" to "করে".split(" "),
        "একটু" to "অপেক্ষা দেখো পরে দাঁড়াও সময়".split(" "),
        "আমার" to "নাম ফোন মনে কাছে একটা কোনো সাথে".split(" "),
        "তোমার" to "নাম ফোন কি সাথে কাছে".split(" "),
        "আপনার" to "নাম ফোন কি সাথে সময়".split(" "),
        "ভাই" to "কেমন আছো আমি কোথায়".split(" "),
        "আপু" to "কেমন আছো আমি".split(" "),
        "স্যার" to "আমি আপনি একটু".split(" "),
        "কিরে" to "কেমন আছিস কি করছিস".split(" "),
        "কখন" to "আসবে আসবেন যাবে হবে আসবা".split(" "),
        "কেন" to "এমন করছো করলে হলো বললে".split(" "),
        "কত" to "দাম টাকা দিন সময় দূর".split(" "),
        "আমরা" to "সবাই একসাথে যাবো আজ কাল".split(" "),
        "তারা" to "সবাই আসবে কেমন কি".split(" "),
        "এখন" to "কি কোথায় আসছি যাচ্ছি আমি".split(" "),
        "এখনই" to "আসছি যাচ্ছি দেখো".split(" "),
        "সবাই" to "ভালো আছে আছেন মিলে আছি আছো কেমন".split(" "),
        "আজকে" to "কি কেমন আমি আর কী".split(" "),
        "চলো" to "যাই দেখি খাই ঘুরি".split(" "),
        "চলুন" to "যাই দেখি খাই ঘুরি".split(" "),
        "যাচ্ছি" to "না আমি এখন একটু".split(" "),
        "আসছি" to "একটু এখনই দাঁড়াও আমি".split(" "),
        "পারবো" to "না আমি আজ".split(" "),
        "লাগবে" to "না কিছু আর কি".split(" "),
        "আর" to "কি কিছু না একটু".split(" "),
        "কিন্তু" to "আমি তুমি আজ".split(" "),
        "তাহলে" to "আমি তুমি চলো আমরা কি".split(" "),
        "অবশ্যই" to "আসবো যাবো করবো আমি".split(" "),
        "ভালোবাসি" to "তোমাকে তোমায়".split(" "),
        "মিস" to "করছি করি করছি করছো".split(" "),
        "ঘুম" to "থেকে আসছে ভালো এসেছে হয়েছে".split(" "),
        "খাওয়া" to "হয়েছে দাওয়া হলো".split(" "),
        "খেয়েছো" to "কি নাকি".split(" "),
        "খেয়েছেন" to "কি নাকি".split(" "),
        "কাজ" to "আছে করছি হয়েছে শেষ".split(" "),
        "বাসায়" to "আছি আছো আছেন যাচ্ছি যাবো আসো এসো ফিরেছি".split(" "),
        "অফিসে" to "আছি আছো যাচ্ছি আসবো".split(" "),
        "টাকা" to "পাঠাও পাঠিয়েছি দিয়েছি লাগবে পাঠাবো দাও".split(" "),
        "ফোন" to "করো করবো দাও নম্বর ধরো".split(" "),
        "মেসেজ" to "করো দিও দেখো দাও দিয়েছি".split(" "),
        "দোয়া" to "করবেন করো রাখবেন রইলো".split(" "),
        "আলহামদুলিল্লাহ" to "ভালো আছি".split(" "),
        "সুবহানাল্লাহ" to "অনেক সুন্দর".split(" "),
        "মাশাআল্লাহ" to "অনেক সুন্দর".split(" "),
        "জাযাকাল্লাহ" to "খাইরান".split(" "),
        "আমীন" to "ইয়া".split(" "),
        "দুঃখিত" to "আমি অনেক".split(" "),
        "সরি" to "আমি অনেক".split(" "),
        "তোমরা" to "কেমন কোথায় সবাই".split(" "),
        "সে" to "কি কেমন আজ কাল".split(" "),
        "এই" to "যে কাজ সময় ভাবে".split(" "),
        "এটা" to "কি কেমন ঠিক".split(" "),
        "ওটা" to "কি কেমন".split(" "),
        "ওরা" to "কি কোথায়".split(" "),
        "কে" to "আসছে করছে".split(" "),
        "কোন" to "সময় জায়গা দিকে".split(" "),
        "কীভাবে" to "করবো হবে যাবো".split(" "),
        "কিভাবে" to "করবো হবে যাবো".split(" "),
        "যদি" to "তুমি আমি পারো".split(" "),
        "এবং" to "আমি".split(" "),
        "তবে" to "আমি তুমি".split(" "),
        "জি" to "আমি ঠিক আছে".split(" "),
        "আসসালামুআলাইকুম" to "ভাই কেমন".split(" "),
        "ওয়ালাইকুম" to "আসসালাম".split(" "),
        "বিসমিল্লাহ" to "হির রাহমানির".split(" "),
        "অভিনন্দন" to "তোমাকে আপনাকে".split(" "),
        "মন" to "খারাপ ভালো".split(" "),
        "খারাপ" to "লাগছে".split(" "),
        "ভাল" to "আছি আছো".split(" "),
        "কথা" to "বলো হবে বলবো".split(" "),
        "বলো" to "কি".split(" "),
        "বলুন" to "কি".split(" "),
        "দেখো" to "আমি".split(" "),
        "আসবো" to "কাল".split(" "),
        "যাবো" to "কাল".split(" "),
        "করবো" to "কাল".split(" "),
        "খাবার" to "খেয়েছো খেয়েছেন তৈরি".split(" "),
        "রাতে" to "দেখা কথা ঘুম".split(" "),
        "সকালে" to "দেখা কথা উঠেছি".split(" "),
        "বিকালে" to "দেখা কথা".split(" "),
        "সন্ধ্যায়" to "দেখা কথা".split(" "),
        "পরশু" to "দেখা হবে".split(" "),
        "আগামীকাল" to "দেখা হবে".split(" "),
        "পরে" to "কথা দেখা কল বলবো".split(" "),
        "একটা" to "কাজ কথা".split(" "),
        "অপেক্ষা" to "করো করুন".split(" "),
        "কল" to "করো দাও করবো দিয়েছিলাম".split(" "),
        "স্কুলে" to "যাচ্ছি আছি যাবো".split(" "),
        "কলেজে" to "যাচ্ছি আছি".split(" "),
        "সময়" to "নেই আছে হবে লাগবে".split(" "),
        "সমস্যা" to "নেই হচ্ছে হয়েছে".split(" "),
        "চিন্তা" to "করো করবেন".split(" "),
        "মাফ" to "করবেন করে করো".split(" "),
        "আচ্ছা" to "ঠিক আছে".split(" "),
        "ঠিকাছে" to "আমি".split(" "),
        "হ্যালো" to "কেমন আছো".split(" "),
        "হাই" to "কেমন আছো".split(" "),
        "বন্ধু" to "কেমন তোমাকে আমার".split(" "),
        "আম্মু" to "কোথায় কেমন".split(" "),
        "আব্বু" to "কোথায় কেমন".split(" "),
        "মা" to "কেমন কোথায় বাবা".split(" "),
        "বাবা" to "কেমন কোথায়".split(" "),
        "ভাইয়া" to "কেমন".split(" "),
        "দোস্ত" to "কেমন".split(" "),
        "ম্যাডাম" to "আমি".split(" "),
        "প্রিয়" to "বন্ধু".split(" "),
        "সবাইকে" to "শুভেচ্ছা সালাম".split(" "),
        "সুন্দর" to "দিন".split(" "),
        "দিন" to "আমাকে".split(" "),
        "রাত" to "টা".split(" "),
        "টা" to "বাজে".split(" "),
        "বাজে" to "আমি".split(" "),
        "নেই" to "কিছু".split(" "),
        "কিছু" to "না নেই".split(" "),
        "কিছুই" to "না".split(" "),
        "কোনো" to "সমস্যা কথা".split(" "),
        "হবে" to "না কি".split(" "),
        "হয়েছে" to "কি".split(" "),
        "হয়" to "না কি".split(" "),
        "হলো" to "কি".split(" "),
        "চল" to "যাই".split(" "),
        "যাই" to "তাহলে".split(" "),
        "দাও" to "আমাকে".split(" "),
        "নাও" to "এটা".split(" "),
        "নিন" to "এটা".split(" "),
        "পাঠাও" to "আমাকে".split(" "),
        "পাঠান" to "আমাকে".split(" "),
        "জানাও" to "আমাকে".split(" "),
        "জানাবেন" to "আমাকে".split(" "),
        "জানাই" to "শুভেচ্ছা".split(" "),
        "আশা" to "করি করছি".split(" "),
        "লাগছে" to "না".split(" "),
        "লাগে" to "না".split(" "),
        "পারি" to "না".split(" "),
        "পারো" to "কি".split(" "),
        "পারেন" to "কি".split(" "),
        "পারবে" to "কি".split(" "),
        "পারছি" to "না".split(" "),
        "চাই" to "না".split(" "),
        "চাও" to "কি".split(" "),
        "চান" to "কি".split(" "),
        "চাইলে" to "তুমি".split(" "),
        "জানি" to "না".split(" "),
        "জানো" to "কি".split(" "),
        "জানেন" to "কি".split(" "),
        "বুঝি" to "না".split(" "),
        "বুঝেছি" to "আমি".split(" "),
        "বুঝলাম" to "না".split(" "),
        "ভাবছি" to "আমি".split(" "),
        "ভাবো" to "কি".split(" "),
        "মনে" to "হচ্ছে হয় আছে".split(" "),
        "দেখছি" to "আমি".split(" "),
        "শুনছি" to "আমি".split(" "),
        "শুনো" to "আমি".split(" "),
        "শোনো" to "আমি".split(" "),
        "শুনুন" to "আমি".split(" "),
        "আসো" to "কাল".split(" "),
        "আসুন" to "কাল".split(" "),
        "যাও" to "কাল".split(" "),
        "যান" to "কাল".split(" "),
        "থাকো" to "ভালো".split(" "),
        "থাকুন" to "ভালো".split(" "),
        "থেকে" to "আমি".split(" "),
        "পর্যন্ত" to "আমি".split(" "),
        "জন্য" to "ধন্যবাদ দোয়া".split(" "),
        "সাথে" to "আছি দেখা কথা".split(" "),
        "সঙ্গে" to "আছি".split(" "),
        "মধ্যে" to "আমি".split(" "),
        "কাছে" to "আছি আসো আছে".split(" "),
        "বাইরে" to "আছি".split(" "),
        "ভেতরে" to "আছি".split(" "),
        "উপর" to "নিচ".split(" "),
        "নিচে" to "আছি".split(" "),
        "সামনে" to "আছি".split(" "),
        "পিছনে" to "আছি".split(" "),
        "বাংলাদেশ" to "এর".split(" "),
        "ঢাকা" to "যাচ্ছি আছি".split(" "),
        "চট্টগ্রাম" to "যাচ্ছি আছি".split(" "),
        "বাংলা" to "ভাষা".split(" "),
        "ভাষা" to "দিবস".split(" "),
        "স্বাধীনতা" to "দিবস".split(" "),
        "বিজয়" to "দিবস".split(" "),
        "রোজা" to "রাখবো".split(" "),
        "নামাজ" to "পড়েছো".split(" "),
        "জুম্মার" to "নামাজ".split(" "),
        "জুমার" to "নামাজ".split(" "),
        "রমজান" to "মুবারক".split(" "),
        "মুবারক" to "হোক".split(" "),
        "আল্লাহর" to "রহমতে ইচ্ছায় কসম".split(" "),
        "আল্লাহু" to "আকবার".split(" "),
        "বারাকাল্লাহ" to "ফিক".split(" "),
        "বিদায়" to "বন্ধু".split(" "),
        "বিকাশে" to "পাঠাও পাঠিয়েছি টাকা".split(" "),
        "নগদে" to "পাঠাও পাঠিয়েছি".split(" "),
        "রিচার্জ" to "করো করেছি".split(" "),
        "ব্যালেন্স" to "নেই আছে".split(" "),
        "নেট" to "নেই নাই চলে".split(" "),
        "নেটওয়ার্ক" to "নেই সমস্যা".split(" "),
        "ফোনে" to "কথা বলবো".split(" "),
        "ছবি" to "পাঠাও পাঠিয়েছি দাও তুলেছি".split(" "),
        "ভিডিও" to "কল দেখেছো পাঠাও".split(" "),
        "অফিস" to "থেকে আছি যাচ্ছি".split(" "),
        "বাড়িতে" to "আছি আছো ফিরেছি".split(" "),
        "বাড়ি" to "যাচ্ছি আসো ফিরেছি".split(" "),
        "ক্লাস" to "আছে নেই শুরু".split(" "),
        "পরীক্ষা" to "ভালো হয়েছে কেমন দিয়েছো আছে".split(" "),
        "পরীক্ষার" to "ফলাফল প্রস্তুতি".split(" "),
        "রেজাল্ট" to "কেমন দিয়েছে হয়েছে".split(" "),
        "ভাত" to "খেয়েছো খেয়েছেন খাবো".split(" "),
        "শরীর" to "ভালো খারাপ কেমন".split(" "),
        "মাথা" to "ব্যথা ঘুরছে".split(" "),
        "জ্বর" to "এসেছে হয়েছে আছে".split(" "),
        "ডাক্তার" to "দেখিয়েছো দেখাবো".split(" "),
        "ওষুধ" to "খেয়েছো খেয়ে".split(" "),
        "কালকে" to "দেখা কখন আসবো".split(" "),
        "দোকানে" to "যাচ্ছি আছি".split(" "),
        "বাজারে" to "যাচ্ছি গিয়েছিলাম".split(" "),
        "বাজার" to "করতে করেছি".split(" "),
        "গাড়ি" to "পাচ্ছি পাইনি আছে".split(" "),
        "বাস" to "পাইনি আছে".split(" "),
        "রাস্তায়" to "আছি জ্যাম".split(" "),
        "জ্যামে" to "আছি পড়েছি".split(" "),
        "জ্যাম" to "আছে".split(" "),
        "দেরি" to "হবে হচ্ছে হয়ে".split(" "),
        "কাজে" to "আছি যাচ্ছি".split(" "),
        "ব্যস্ত" to "আছি আছো".split(" "),
        "ভাবি" to "কেমন আছেন".split(" "),
        "খালা" to "কেমন আছেন".split(" "),
        "চাচা" to "কেমন আছেন".split(" "),
        "মামা" to "কেমন আছেন".split(" "),
        "ফুফু" to "কেমন আছেন".split(" "),
        "পছন্দ" to "হয়েছে করি".split(" "),
        "ভয়" to "পাচ্ছি পাই".split(" "),
        "রাগ" to "করো করেছো".split(" "),
        "প্লিজ" to "বলো দাও".split(" "),
        "সত্যি" to "বলছো নাকি".split(" "),
        "নাকি" to "তুমি আপনি".split(" "),
        "ওকে" to "ঠিক আছে".split(" "),
        "নাম" to "কি".split(" "),
        "ঈদের" to "শুভেচ্ছা দাওয়াত ছুটি".split(" "),
        "ছুটি" to "পেয়েছি আছে".split(" "),
        "দাওয়াত" to "রইলো দিচ্ছি".split(" "),
        "বিয়ে" to "করেছি হবে".split(" "),
        "বিয়ের" to "দাওয়াত".split(" "),
        "নতুন" to "বছরের বছর ফোন".split(" "),
        "বছরের" to "শুভেচ্ছা".split(" "),
        "হ্যাপি" to "বার্থডে নিউ".split(" "),
        "গুড" to "মর্নিং নাইট".split(" "),
        "মসজিদে" to "যাচ্ছি যাবো".split(" "),
        "মসজিদ" to "থেকে".split(" "),
        "কুরআন" to "পড়ো পড়ি তিলাওয়াত".split(" "),
        "ইফতার" to "করেছো করবো".split(" "),
        "সেহরি" to "খেয়েছো".split(" "),
        "হজ্জ" to "করতে করবো".split(" "),
        "রাসূল" to "সাল্লাল্লাহু".split(" "),
        "আছো" to "নাকি কেমন".split(" "),
        "আছেন" to "নাকি কেমন".split(" "),
        "করছো" to "কি কী".split(" "),
        "করছেন" to "কি কী".split(" "),
        "ঘুমিয়ে" to "পড়েছি পড়ো পড়েছো".split(" "),
        "পৌঁছে" to "গেছি গেছো যাবো".split(" "),
        "পৌঁছেছি" to "একটু".split(" "),
        "এসো" to "ঘরে বসো".split(" "),
        "বসো" to "এখানে".split(" "),
        "আলাইকুম" to "আসসালাম".split(" ")
    )
}
