# Routine artwork

The active eight-movement routine (version 5) uses only adult Saiyans, with two movements each for Goku, Vegeta, Gohan, and Future Trunks. No child characters are used. Every movement has a base-form PNG and a Super Saiyan PNG. Completing one full loop within a single workout day unlocks the transformed set.

## Asset assignments

| Character | Movement | Base artwork | Super Saiyan artwork |
| --- | --- | --- | --- |
| Trunks | Dead hang | [movement_dead_hang.png](../app/src/main/res/drawable-nodpi/movement_dead_hang.png) | [movement_dead_hang_ssj.png](../app/src/main/res/drawable-nodpi/movement_dead_hang_ssj.png) |
| Goku | Deep squat hold | [movement_deep_squat.png](../app/src/main/res/drawable-nodpi/movement_deep_squat.png) | [movement_deep_squat_ssj.png](../app/src/main/res/drawable-nodpi/movement_deep_squat_ssj.png) |
| Trunks | Band pull-aparts | [movement_band_pull_aparts.png](../app/src/main/res/drawable-nodpi/movement_band_pull_aparts.png) | [movement_band_pull_aparts_ssj.png](../app/src/main/res/drawable-nodpi/movement_band_pull_aparts_ssj.png) |
| Gohan | Three-point bridge | [movement_three_point_bridge.png](../app/src/main/res/drawable-nodpi/movement_three_point_bridge.png) | [movement_three_point_bridge_ssj.png](../app/src/main/res/drawable-nodpi/movement_three_point_bridge_ssj.png) |
| Gohan | Scapular pull-ups | [movement_scapular_pullups.png](../app/src/main/res/drawable-nodpi/movement_scapular_pullups.png) | [movement_scapular_pullups_ssj.png](../app/src/main/res/drawable-nodpi/movement_scapular_pullups_ssj.png) |
| Vegeta | Hip-flexor stretch | [movement_hip_flexor.png](../app/src/main/res/drawable-nodpi/movement_hip_flexor.png) | [movement_hip_flexor_ssj.png](../app/src/main/res/drawable-nodpi/movement_hip_flexor_ssj.png) |
| Vegeta | Squats | [movement_squats.png](../app/src/main/res/drawable-nodpi/movement_squats.png) | [movement_squats_ssj.png](../app/src/main/res/drawable-nodpi/movement_squats_ssj.png) |
| Goku | Bear plank hold | [movement_bear_plank.png](../app/src/main/res/drawable-nodpi/movement_bear_plank.png) | [movement_bear_plank_ssj.png](../app/src/main/res/drawable-nodpi/movement_bear_plank_ssj.png) |

All assets are saved under `app/src/main/res/drawable-nodpi/`. Routine 5 adds Future Trunks illustrations for band pull-aparts, upgrades hip-flexor stretch from Tien to adult Vegeta (base + Super Saiyan), and updates deep squat hold to adult Goku (base + Super Saiyan) to maintain an even 2-2-2-2 balance across the four adult Saiyans. Older artwork remains available for retired movements. Start and end poses occupy the left and right panels of each opaque image. For timed holds, END shows the position to hold. Character identity, exercise form, equipment contacts, panel cropping and transformation were visually reviewed before integration.

The edit targets were the previous movement illustrations. Original adult Goku, Vegeta, Gohan and Future Trunks artwork supplied the character references. The supported deadlift and reverse lunge illustrations were subsequently edited in both forms to replace the child figures with adult Goku and Vegeta, respectively. Reference filenames below identify the inputs as they existed before this replacement.

## Generation prompts

The six new routine 3 assets and their exact prompts are documented in [adapted-routine-artwork.md](adapted-routine-artwork.md). The prompts below document the retained and historical assets.

### movement_pike_pushups.png

Saved to `app/src/main/res/drawable-nodpi/movement_pike_pushups.png`.

Inputs: `movement_pike_pushups.png`, `movement_pushups.png`.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration for an Android app. Image 1 is the EDIT TARGET showing Piccolo doing pike_pushups; Image 2 is a CHARACTER REFERENCE for Goku only. Replace Piccolo with Goku IN BOTH PANELS, matching the character reference: black spiky Goku hair, natural skin, orange gi with blue undershirt, blue sash and wristbands, dark blue boots with red trim. Remove all green skin, pointy Namekian ears, turban, purple gi and Piccolo traits. Preserve the exact exercise, body poses, hand and foot contacts, equipment, white background, cel-shaded illustration style, two equal side-by-side panels and center divider of image 1. Do not copy the exercise pose from image 2. Keep each complete figure fully within its half with safe margins, no text or watermarks. Pike push-ups: keep hips high in inverted V; left straight arms, right elbows bent with head near floor slightly ahead of hands.

### movement_pike_pushups_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_pike_pushups_ssj.png`.

Inputs: `movement_pike_pushups.png`, `movement_pushups_ssj.png`.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration for an Android app. Image 1 is the EDIT TARGET showing Piccolo doing pike_pushups; Image 2 is a CHARACTER REFERENCE for Super Saiyan Goku only. Replace Piccolo with Super Saiyan Goku IN BOTH PANELS, matching the character reference: upright golden Super Saiyan hair, teal eyes, natural skin, orange gi with blue undershirt, blue sash and wristbands, dark blue boots with red trim, subtle golden aura. Remove all green skin, pointy Namekian ears, turban, purple gi and Piccolo traits. Preserve the exact exercise, body poses, hand and foot contacts, equipment, white background, cel-shaded illustration style, two equal side-by-side panels and center divider of image 1. Do not copy the exercise pose from image 2. Keep each complete figure fully within its half with safe margins, no text or watermarks. Pike push-ups: keep hips high in inverted V; left straight arms, right elbows bent with head near floor slightly ahead of hands.

### movement_tuck_l_sit.png

Saved to `app/src/main/res/drawable-nodpi/movement_tuck_l_sit.png`.

Inputs: `movement_tuck_l_sit.png`, `movement_squats.png`.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration for an Android app. Image 1 is the EDIT TARGET showing Piccolo doing tuck_l_sit; Image 2 is a CHARACTER REFERENCE for Vegeta only. Replace Piccolo with Vegeta IN BOTH PANELS, matching the character reference: upright black hair with widow's peak, natural skin, blue bodysuit, white and gold Saiyan armor, white gloves, white and gold boots. Remove all green skin, pointy Namekian ears, turban, purple gi and Piccolo traits. Preserve the exact exercise, body poses, hand and foot contacts, equipment, white background, cel-shaded illustration style, two equal side-by-side panels and center divider of image 1. Do not copy the exercise pose from image 2. Keep each complete figure fully within its half with safe margins, no text or watermarks. Tuck L-sit: left upright straight-arm support with toes lightly on floor; right same straight arms with knees tucked and feet suspended; preserve both stable parallel bars.

### movement_tuck_l_sit_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_tuck_l_sit_ssj.png`.

Inputs: `movement_tuck_l_sit.png`, `movement_squats_ssj.png`.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration for an Android app. Image 1 is the EDIT TARGET showing Piccolo doing tuck_l_sit; Image 2 is a CHARACTER REFERENCE for Super Saiyan Vegeta only. Replace Piccolo with Super Saiyan Vegeta IN BOTH PANELS, matching the character reference: upright golden Super Saiyan hair with widow's peak, teal eyes, natural skin, blue bodysuit, white and gold Saiyan armor, white gloves, white and gold boots, subtle golden aura. Remove all green skin, pointy Namekian ears, turban, purple gi and Piccolo traits. Preserve the exact exercise, body poses, hand and foot contacts, equipment, white background, cel-shaded illustration style, two equal side-by-side panels and center divider of image 1. Do not copy the exercise pose from image 2. Keep each complete figure fully within its half with safe margins, no text or watermarks. Tuck L-sit: left upright straight-arm support with toes lightly on floor; right same straight arms with knees tucked and feet suspended; preserve both stable parallel bars.

### movement_three_point_bridge.png

Saved to `app/src/main/res/drawable-nodpi/movement_three_point_bridge.png`.

Inputs: `movement_three_point_bridge.png`, `movement_deep_squat.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for three_point_bridge. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Gohan. Character: Gohan in purple sleeveless gi, red sash and red wristbands, olive boots with dark toes, natural human skin; black spiky Gohan hair with a front bang. Base form with BLACK hair for Gohan/Goten or LAVENDER hair for Trunks. No aura. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Three-point bridge: both feet and one hand planted; left hips low in crab preparation, right hips lifted and free arm reaching overhead. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

### movement_three_point_bridge_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_three_point_bridge_ssj.png`.

Inputs: `movement_three_point_bridge.png`, `movement_deep_squat_ssj.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for three_point_bridge. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Super Saiyan Gohan. Character: Gohan in purple sleeveless gi, red sash and red wristbands, olive boots with dark toes, natural human skin; black spiky Gohan hair with a front bang. TRANSFORM BOTH figures: upright GOLDEN Super Saiyan hair, teal eyes and a restrained golden aura. Preserve the distinctive character face and outfit. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Three-point bridge: both feet and one hand planted; left hips low in crab preparation, right hips lifted and free arm reaching overhead. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

The chin-up prompts below describe the preserved historical assets. Routine 4 uses [scapular pull-up artwork and prompts](scapular-pullup-artwork.md).

### movement_pullups.png

Saved to `app/src/main/res/drawable-nodpi/movement_pullups.png`.

Inputs: `movement_pullups.png`, `movement_deep_squat.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for pullups. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Gohan. Character: Gohan in purple sleeveless gi, red sash and red wristbands, olive boots with dark toes, natural human skin; black spiky Gohan hair with a front bang. Base form with BLACK hair for Gohan/Goten or LAVENDER hair for Trunks. No aura. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Chin-up with palms facing the character: left straight-arm dead hang, right chin just above bar and elbows bent. Preserve stable pull-up bar and full body. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

Final framing and chin-up pose correction:

Use case: precise-object-edit. Correct the framing and chin-up end pose of this Gohan fitness illustration. Preserve Gohan's exact identity, outfit, base or Super Saiyan form, drawing style, white backdrop and two equal side-by-side panels. The full figure including ALL hair must fit inside each panel with at least 50 pixels of white space above the tallest hair and comfortable space below the boots; zoom the composition out enough to achieve this. LEFT panel: arms straight in full dead hang, palms facing Gohan. RIGHT panel: show a completed CHIN-UP with the underside of his CHIN CLEARLY ABOVE the horizontal bar, not his nose or mouth at bar height; elbows bent and pulled down. Position the bar lower within the right panel as needed so that all head and hair fit with margin. Keep the bar supported on both sides. Correct anatomy, consistent scale, no clipped head, hair, feet or limbs. No text or watermarks.

### movement_pullups_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_pullups_ssj.png`.

Inputs: `movement_pullups.png`, `movement_deep_squat_ssj.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for pullups. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Super Saiyan Gohan. Character: Gohan in purple sleeveless gi, red sash and red wristbands, olive boots with dark toes, natural human skin; black spiky Gohan hair with a front bang. TRANSFORM BOTH figures: upright GOLDEN Super Saiyan hair, teal eyes and a restrained golden aura. Preserve the distinctive character face and outfit. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Chin-up with palms facing the character: left straight-arm dead hang, right chin just above bar and elbows bent. Preserve stable pull-up bar and full body. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

Final framing and chin-up pose correction:

Use case: precise-object-edit. Correct the framing and chin-up end pose of this Gohan fitness illustration. Preserve Gohan's exact identity, outfit, base or Super Saiyan form, drawing style, white backdrop and two equal side-by-side panels. The full figure including ALL hair must fit inside each panel with at least 50 pixels of white space above the tallest hair and comfortable space below the boots; zoom the composition out enough to achieve this. LEFT panel: arms straight in full dead hang, palms facing Gohan. RIGHT panel: show a completed CHIN-UP with the underside of his CHIN CLEARLY ABOVE the horizontal bar, not his nose or mouth at bar height; elbows bent and pulled down. Position the bar lower within the right panel as needed so that all head and hair fit with margin. Keep the bar supported on both sides. Correct anatomy, consistent scale, no clipped head, hair, feet or limbs. No text or watermarks.

### movement_ring_rows.png

Saved to `app/src/main/res/drawable-nodpi/movement_ring_rows.png`.

Inputs: `movement_ring_rows.png`, `movement_dead_hang.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for ring_rows. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Trunks. Character: Future Trunks, lavender center-parted hair, short blue jacket over black shirt, dark trousers, tan boots with dark toes and a belt; natural human skin. Base form with BLACK hair for Gohan/Goten or LAVENDER hair for Trunks. No aura. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Ring row: both heels stay planted, body straight and leaning back; left arms straight holding rings, right elbows bent and rings drawn to chest. Straps and wooden rings unchanged. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

### movement_ring_rows_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_ring_rows_ssj.png`.

Inputs: `movement_ring_rows.png`, `movement_dead_hang_ssj.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for ring_rows. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Super Saiyan Trunks. Character: Future Trunks, lavender center-parted hair, short blue jacket over black shirt, dark trousers, tan boots with dark toes and a belt; natural human skin. TRANSFORM BOTH figures: upright GOLDEN Super Saiyan hair, teal eyes and a restrained golden aura. Preserve the distinctive character face and outfit. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Ring row: both heels stay planted, body straight and leaning back; left arms straight holding rings, right elbows bent and rings drawn to chest. Straps and wooden rings unchanged. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

### movement_ring_hamstring_curls.png

Saved to `app/src/main/res/drawable-nodpi/movement_ring_hamstring_curls.png`.

Inputs: `movement_ring_hamstring_curls.png`, `movement_dead_hang.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for ring_hamstring_curls. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Trunks. Character: Future Trunks, lavender center-parted hair, short blue jacket over black shirt, dark trousers, tan boots with dark toes and a belt; natural human skin. Base form with BLACK hair for Gohan/Goten or LAVENDER hair for Trunks. No aura. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Hamstring curl: shoulders and arms remain on floor, both heels supported in TWO low rings; left knees nearly straight and hips lifted; right knees bent and heels drawn closer to hips. Hands remain on floor, not rings. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

### movement_ring_hamstring_curls_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_ring_hamstring_curls_ssj.png`.

Inputs: `movement_ring_hamstring_curls.png`, `movement_dead_hang_ssj.png`.

Use case: precise-object-edit. Asset type: paired start/end exercise artwork for Android app. Image 1 is EDIT TARGET for ring_hamstring_curls. Image 2 is a CHARACTER/CLOTHING REFERENCE ONLY. Replace the figure in BOTH PANELS with Super Saiyan Trunks. Character: Future Trunks, lavender center-parted hair, short blue jacket over black shirt, dark trousers, tan boots with dark toes and a belt; natural human skin. TRANSFORM BOTH figures: upright GOLDEN Super Saiyan hair, teal eyes and a restrained golden aura. Preserve the distinctive character face and outfit. Remove all green skin, Namekian ears, turban, purple Piccolo costume or Krillin forehead dots. Preserve the exercise in image 1, NOT the exercise in image 2. Hamstring curl: shoulders and arms remain on floor, both heels supported in TWO low rings; left knees nearly straight and hips lifted; right knees bent and heels drawn closer to hips. Hands remain on floor, not rings. Keep the same hand and foot contacts, equipment, accurate joint positions, white background and light ground shadows, clean anime linework and cel shading. Landscape two EQUAL side-by-side panels with divider exactly at center. One complete full-body figure per half, safely inside its half. No text, logos, watermarks, extra characters or extra limbs.

Final framing correction:

Use case: precise-object-edit. Adjust framing ONLY in this two-panel Super Saiyan Trunks ring hamstring curl illustration. Zoom out both panels evenly by about 12 percent so that the ENTIRE golden hair and full body of each figure have comfortable white margins; in particular, the LEFT figure's hair must be fully visible with at least 50 pixels between its leftmost hair tip and the canvas edge. Keep the divider exactly centered, the exact character, golden hair, teal eyes, blue jacket, black trousers, tan boots, restrained golden aura, equipment, start/end exercise poses, hand and heel contacts, white background and drawing style unchanged. Do not crop any hair or feet. No text or watermarks.

### movement_single_leg_rdl.png

Saved to `app/src/main/res/drawable-nodpi/movement_single_leg_rdl.png`.

Inputs: `movement_single_leg_rdl.png`, `movement_pushups.png`. The first image was the prior child illustration; the second supplied the adult character reference.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration in an Android app. Image 1 is the EDIT TARGET. Image 2 is a CHARACTER REFERENCE ONLY for adult Goku. Replace the child figure in BOTH panels with ADULT Goku, base form with BLACK spiky hair, dark eyes and no aura, wearing orange gi, SHORT blue undershirt sleeves exposing muscular adult upper arms, blue sash and wristbands, blue boots with red trim. Match the reference's mature facial structure, small adult eyes, muscular broad torso, long adult limbs and adult head-to-body proportions. No child or chibi proportions. Preserve exercise from image 1, never copy the reference's exercise pose. Supported single-leg Romanian deadlift: left upright balance with one hand lightly on sturdy support, free foot lifted; right hips hinge backward, flat long back with torso nearly horizontal, free leg extending backward and same supporting hand on table, standing knee softly bent. Keep full support table visible. Preserve vertical separator at x=704 on a 1536px wide canvas (11/24 width), with narrower start panel and wider end panel. Keep original pose directions, hand/foot contact points, support equipment, white backdrop, subtle ground shadow and clean cel-shaded anime linework. Fit the ENTIRE figure, all hair, hands and feet inside each panel with generous white margins, scaling to fit rather than clipping. No text, labels, logos, watermarks, extra figures or extra limbs. Output landscape 1536x1024.

### movement_single_leg_rdl_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_single_leg_rdl_ssj.png`.

Inputs: `movement_single_leg_rdl_ssj.png`, `movement_pushups_ssj.png`. The first image was the prior child illustration; the second supplied the adult character reference.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration in an Android app. Image 1 is the EDIT TARGET. Image 2 is a CHARACTER REFERENCE ONLY for adult Goku. Replace the child figure in BOTH panels with ADULT Goku, Super Saiyan with GOLDEN upright hair, teal eyes and restrained golden aura, wearing orange gi, SHORT blue undershirt sleeves exposing muscular adult upper arms, blue sash and wristbands, blue boots with red trim. Match the reference's mature facial structure, small adult eyes, muscular broad torso, long adult limbs and adult head-to-body proportions. No child or chibi proportions. Preserve exercise from image 1, never copy the reference's exercise pose. Supported single-leg Romanian deadlift: left upright balance with one hand lightly on sturdy support, free foot lifted; right hips hinge backward, flat long back with torso nearly horizontal, free leg extending backward and same supporting hand on table, standing knee softly bent. Keep full support table visible. Preserve vertical separator at x=704 on a 1536px wide canvas (11/24 width), with narrower start panel and wider end panel. Keep original pose directions, hand/foot contact points, support equipment, white backdrop, subtle ground shadow and clean cel-shaded anime linework. Fit the ENTIRE figure, all hair, hands and feet inside each panel with generous white margins, scaling to fit rather than clipping. No text, labels, logos, watermarks, extra figures or extra limbs. Output landscape 1536x1024.

### movement_reverse_lunges.png

Saved to `app/src/main/res/drawable-nodpi/movement_reverse_lunges.png`.

Inputs: `movement_reverse_lunges.png`, `movement_squats.png`. The first image was the prior child illustration; the second supplied the adult character reference.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration in an Android app. Image 1 is the EDIT TARGET. Image 2 is a CHARACTER REFERENCE ONLY for adult Vegeta. Replace the child figure in BOTH panels with ADULT Vegeta, base form with BLACK spiky hair, dark eyes and no aura, wearing blue bodysuit, white and gold Saiyan chest armor, white gloves and white/gold boots; sharp widow's peak and stern adult face. Match the reference's mature facial structure, small adult eyes, muscular broad torso, long adult limbs and adult head-to-body proportions. No child or chibi proportions. Preserve exercise from image 1, never copy the reference's exercise pose. Reverse lunge: left stands tall feet together, right steps one foot back, both knees bending with rear knee hovering near floor, front foot flat. Preserve two equal panels with vertical divider at midpoint. Keep original pose directions, hand/foot contact points, support equipment, white backdrop, subtle ground shadow and clean cel-shaded anime linework. Fit the ENTIRE figure, all hair, hands and feet inside each panel with generous white margins, scaling to fit rather than clipping. No text, labels, logos, watermarks, extra figures or extra limbs. Output landscape 1536x1024.

### movement_reverse_lunges_ssj.png

Saved to `app/src/main/res/drawable-nodpi/movement_reverse_lunges_ssj.png`.

Inputs: `movement_reverse_lunges_ssj.png`, `movement_squats_ssj.png`. The first image was the prior child illustration; the second supplied the adult character reference.

Use case: precise-object-edit. Asset type: paired start/end fitness illustration in an Android app. Image 1 is the EDIT TARGET. Image 2 is a CHARACTER REFERENCE ONLY for adult Vegeta. Replace the child figure in BOTH panels with ADULT Vegeta, Super Saiyan with GOLDEN upright hair, teal eyes and restrained golden aura, wearing blue bodysuit, white and gold Saiyan chest armor, white gloves and white/gold boots; sharp widow's peak and stern adult face. Match the reference's mature facial structure, small adult eyes, muscular broad torso, long adult limbs and adult head-to-body proportions. No child or chibi proportions. Preserve exercise from image 1, never copy the reference's exercise pose. Reverse lunge: left stands tall feet together, right steps one foot back, both knees bending with rear knee hovering near floor, front foot flat. Preserve two equal panels with vertical divider at midpoint. Keep original pose directions, hand/foot contact points, support equipment, white backdrop, subtle ground shadow and clean cel-shaded anime linework. Fit the ENTIRE figure, all hair, hands and feet inside each panel with generous white margins, scaling to fit rather than clipping. No text, labels, logos, watermarks, extra figures or extra limbs. Output landscape 1536x1024.

Final framing correction (built-in imagegen edit, using the first adult Super Saiyan reverse-lunge result as its input):

Use case: precise-object-edit. Fix ONLY framing in this paired Super Saiyan adult Vegeta reverse-lunge illustration. The LEFT figure's golden hair currently touches/crosses the TOP edge. Zoom BOTH figures out by 12 percent around their panel centers, leaving at least 70 pixels of WHITE SPACE ABOVE ALL HAIR in each panel and 70 pixels below boots. Entire hair tips must be visible, none touching any canvas edge. Preserve the exact adult Vegeta identity, mature face, muscular adult proportions, golden hair, teal eyes, subtle golden aura, blue bodysuit, white/gold chest armor, white gloves/boots, start standing pose and end reverse lunge pose, full feet and hand positions, clean anime linework and light floor shadows. Keep two equal side-by-side panels, divider exactly at midpoint, opaque white background, landscape 1536x1024. No text or watermark. Change only scale/framing; do not introduce different characters.

## Routine 5 artwork

Three paired base / Super Saiyan illustrations were added or updated for Routine 5 (`1536x1024` PNGs, equal 50% panel split):

- `movement_band_pull_aparts.png` / `movement_band_pull_aparts_ssj.png` — Adult Future Trunks (referencing `movement_dead_hang.png` / `_ssj.png`) holding a thin red resistance band out front at chest height on the left and pulling it wide across his chest on the right.
- `movement_hip_flexor.png` / `movement_hip_flexor_ssj.png` — Adult Vegeta (referencing `movement_squats.png` / `_ssj.png`), replacing Tien in the half-kneeling hip-flexor stretch pose so every active movement has a Super Saiyan transformation.
- `movement_deep_squat.png` / `movement_deep_squat_ssj.png` — Adult Goku (referencing `movement_pushups.png` / `_ssj.png`), replacing Gohan in the standing-to-deep-squat hold pose to maintain an even 2-2-2-2 split across Goku, Vegeta, Gohan, and Future Trunks.

## Emulator layout verification

All eight active Routine 5 movements split evenly at the midpoint (`1/2`). Restored `single_leg_rdl` artwork retains its `11/24` panel separator in both forms (`MovementArtwork.splitNumerator = 11`, `splitDenominator = 24`). The renderer omits the separator so no sliver of the opposite panel appears in either card.

## Strength routine artwork

Strength A and Strength B reuse exact-match base + Super Saiyan WebP pairs where available and restore four verified historical pairs from git (`movement_heel_dig_bridges`, `movement_single_leg_rdl`, `movement_reverse_lunges`, `movement_tuck_l_sit`):

| Movement | Status | Character | Base / Super Saiyan WebP |
| --- | --- | --- | --- |
| Tempo Bodyweight Squat | Active (reused) | Vegeta | `movement_squats.webp` / `_ssj.webp` |
| Heel-Dig Bridges | Active (restored) | Trunks | `movement_heel_dig_bridges.webp` / `_ssj.webp` |
| Band Pull-Aparts | Active (reused) | Trunks | `movement_band_pull_aparts.webp` / `_ssj.webp` |
| Supported Single-Leg RDL | Active (restored, `11/24` split) | Goku | `movement_single_leg_rdl.webp` / `_ssj.webp` |
| Bodyweight Reverse Lunges | Active (restored) | Vegeta | `movement_reverse_lunges.webp` / `_ssj.webp` |
| Tuck L-Sit | Active (restored) | Vegeta | `movement_tuck_l_sit.webp` / `_ssj.webp` |
| Bear Plank Hold | Active (reused) | Goku | `movement_bear_plank.webp` / `_ssj.webp` |
| Incline Push-ups | Pending image generation | Goku | `movement_incline_pushups.webp` / `_ssj.webp` |
| Parallel Bar Inverted Rows | Pending image generation | Gohan | `movement_parallel_bar_rows.webp` / `_ssj.webp` |
| Kneeling Ab Wheel Rollouts | Pending image generation | Gohan | `movement_ab_wheel_rollouts.webp` / `_ssj.webp` |
| Bear Plank Shoulder Taps | Pending image generation | Trunks | `movement_bear_plank_shoulder_taps.webp` / `_ssj.webp` |
| Band-Assisted Chin-ups | Pending image generation | Gohan | `movement_band_assisted_chinups.webp` / `_ssj.webp` |
