(ns gui.layouts)

(def hud
;; CL Class TE text BC back color FC fore color WI width HE height
;; TA top BA bottom LA left RA right HA horizontal VA vertical align
;; 0 : edges or screen"
"
X             M|
               |
C              |
BJ           LR|
KG    HSP     D|
X CLIndicator TEXP BCFFFFFF55 FCFFFFFFFF TA0 LA0 WI150 HE50
M CLButton TEMenu BCFF00FF55 FCFFFFFFFF TA0 RA0 WI150 HE50 COShowMenu
H CLIndicator TEHealth BCFF000055 FCFFFFFFFF BA0 RAS WI200 HE50
S CLLabel TEBullets BC00FF0055 FCFFFFFFFF BA0 HA0 WI50 HE50
P CLIndicator TEPower BC0000FF55 FCFFFFFFFF BA0 LAS WI200 HE50
B CLButton TEBlock BCFFFFFF55 FCFFFFFFFF BAK LA0 WI100 HE100
K CLButton TEKick BCFFFFFF55 FCFFFFFFFF BA0 LA0 WI100 HE100
C CLButton TEPunch BCFFFFFF55 FCFFFFFFFF BAB LA0 WI100 HE100
G CLButton TEGun BCFFFFFF55 FCFFFFFFFF BA0 LAK WI100 HE100
J CLButton TEJump BCFFFFFF55 FCFFFFFFFF BAG LAB WI100 HE100
L CLButton TELeft BCFFFFFF55 FCFFFFFFFF BA0 RAR WI100 HE100
R CLButton TERight BCFFFFFF55 FCFFFFFFFF BAD RA0 WI100 HE100
D CLButton TEDown BCFFFFFF55 FCFFFFFFFF BA0 RA0 WI100 HE100
E CLDebug TA0 LA0 WI300 HE300
")

       
(def menu
"
 C |
 N |
 O |
 D |
C CLButton TEContinue BCFF00FFFF FCFFFFFFFF BAN HA0 WI250 HE50
N CLButton TENew~Game BCFFFF00FF FCFFFFFFFF BAO HA0 WI250 HE50
O CLButton TEOptions BC00FFFFFF FCFFFFFFFF VA0 HA0 WI250 HE50
D CLButton TEDonate BCFFFFFFFF FCFFFFFFFF TAO HA0 WI250 HE50
")

(def opts
"
 M |
 S |
 A |
 P |
 B |
M CLSlider TEMusic~Volume BCFFFFFF55 FCFFFFFFFF BAS HA0 WI250 HE50
S CLSlider TESound~Volume BCFFFFFF55 FCFFFFFFFF BAA HA0 WI250 HE50
A CLSlider TEControls~Alpha BCFFFFFF55 FCFFFFFFFF VA0 HA0 WI250 HE50
P CLToggle TEShow/Hide~Physics BCFFFFFF55 FCFFFFFFFF TAA HA0 WI250 HE50
B CLButton TEBack BCFFFFFF55 FCFFFFFFFF TAP HA0 WI250 HE50
")
