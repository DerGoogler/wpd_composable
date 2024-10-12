SKIPMOUNT=false
PROPFILE=false
POSTFSDATA=false
LATESTARTSERVICE=false

print_modname() {
    ui_print " __        ______  ____  "
    ui_print " \ \      / |  _ \|  _ \ "
    ui_print "  \ \ /\ / /| |_) | | | |"
    ui_print "   \ V  V / |  __/| |_| |"
    ui_print "    \_/\_/  |_|   |____/ "
}

on_install() {
    is_64bit=false
    case "$(getprop ro.product.cpu.abi)" in
    arm64-v8a | x86_64) is_64bit=true ;;
    esac

    ui_print "- Extracting module files"
    unzip -qq -o "$ZIPFILE" 'system/*' -d $MODPATH >&2
    [ -d "$MODPATH/system/bin/" ] || mkdir -p "$MODPATH/system/bin/"

    if [ "$is_64bit" = true ]; then
        TARGET_DIR="/system/lib64"
    else
        TARGET_DIR="/system/lib"
    fi

    [ -d "$MODPATH$TARGET_DIR" ] || mkdir -p "$MODPATH$TARGET_DIR"

    mv -f mmrl_wpd.apk $MODPATH$TARGET_DIR
}

set_permissions() {
    # The following is the default rule, DO NOT remove
    set_perm_recursive $MODPATH 0 0 0755 0644
    set_perm $MODPATH/system/bin/wpd 0 0 0755
    set_perm $MODPATH$TARGET_DIR/mmrl_wpd.apk 0 0 0444
}
